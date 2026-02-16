package de.bennyboer.kicherkrabbe.messaging.listener;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory.InMemoryMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.RabbitOutboxEntryPublisher;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import de.bennyboer.kicherkrabbe.messaging.testing.RabbitMessagingTest;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@RabbitMessagingTest
public class MessageListenerTests {

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    JsonMapper jsonMapper;

    ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    InMemoryMessagingOutboxRepo outboxRepo = new InMemoryMessagingOutboxRepo();

    RabbitOutboxEntryPublisher publisher;

    MessagingOutbox outbox;

    List<MessageListener> listeners = new ArrayList<>();

    MessageListenerContainerManager containerManager = new MessageListenerContainerManager();

    @BeforeEach
    void setUp() {
        publisher = new RabbitOutboxEntryPublisher(rabbitTemplate, rabbitAdmin, jsonMapper);
        outbox = new MessagingOutbox(outboxRepo, publisher, 10, Clock.systemUTC());
    }

    @AfterEach
    void tearDown() {
        for (MessageListener listener : listeners) {
            listener.destroy();
        }
        listeners.clear();
    }

    @Test
    void shouldNotProcessDuplicateMessages() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-dedup");
        var routingKey = RoutingKey.parse("test.dedup.key");
        var listenerName = "test-dedup-listener";

        var processedCount = new AtomicInteger(0);
        var latch = new CountDownLatch(1);

        var inboxRepo = new InMemoryMessagingInboxRepo(true);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new RabbitMessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            processedCount.incrementAndGet();
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        send(exchange, routingKey, Map.of("test", "data"));

        boolean received = latch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();
        assertThat(processedCount.get()).isEqualTo(1);

        int inboxSize = 0;
        for (int i = 0; i < 10; i++) {
            var inboxMessages = inboxRepo.findAll().collectList().block();
            inboxSize = inboxMessages.size();
            if (inboxSize == 1) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(inboxSize).isEqualTo(1);
    }

    @Test
    void shouldSendFailedMessagesToDeadLetterQueue() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-dlq");
        var routingKey = RoutingKey.parse("test.dlq.key");
        var listenerName = "test-dlq-listener";

        var attemptCount = new AtomicInteger(0);

        var inboxRepo = new InMemoryMessagingInboxRepo(false);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new RabbitMessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            attemptCount.incrementAndGet();
            return Mono.error(new RuntimeException("Simulated failure"));
        });
        listeners.add(listener);
        listener.start();

        send(exchange, routingKey, Map.of("will", "fail"));

        var deadQueueName = "%s-%s-%s-dead".formatted(exchange.getName(), routingKey.asString(), listenerName);
        Message deadMessage = null;
        for (int i = 0; i < 20 && deadMessage == null; i++) {
            Thread.sleep(500);
            deadMessage = rabbitTemplate.receive(deadQueueName, 1000);
        }

        assertThat(deadMessage).isNotNull();
        var body = new String(deadMessage.getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains("\"will\"");
        assertThat(body).contains("\"fail\"");
        assertThat(attemptCount.get()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldFailAfterMaxRetries() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-max-retry");
        var routingKey = RoutingKey.parse("test.maxretry.key");
        var listenerName = "test-maxretry-listener";

        var attemptCount = new AtomicInteger(0);

        var inboxRepo = new InMemoryMessagingInboxRepo(false);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new RabbitMessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            attemptCount.incrementAndGet();
            return Mono.error(new RuntimeException("Persistent failure"));
        });
        listeners.add(listener);
        listener.start();

        send(exchange, routingKey, Map.of("test", "maxretry"));

        var deadQueueName = "%s-%s-%s-dead".formatted(exchange.getName(), routingKey.asString(), listenerName);
        Message deadMessage = null;
        for (int i = 0; i < 20 && deadMessage == null; i++) {
            Thread.sleep(500);
            deadMessage = rabbitTemplate.receive(deadQueueName, 1000);
        }

        assertThat(deadMessage).isNotNull();
        assertThat(attemptCount.get()).isEqualTo(4);
    }

    private void send(ExchangeTarget exchange, RoutingKey routingKey, Map<String, Object> payload) {
        var messageTarget = MessageTarget.exchange(exchange);
        var entry = MessagingOutboxEntry.create(messageTarget, routingKey, payload, Clock.systemUTC());
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        outbox.insert(entry)
                .as(transactionalOperator::transactional)
                .block(Duration.ofSeconds(5));

        outbox.publishNextUnpublishedEntries().block(Duration.ofSeconds(5));
    }

}
