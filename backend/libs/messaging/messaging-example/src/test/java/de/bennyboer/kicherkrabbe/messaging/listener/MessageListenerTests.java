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
import de.bennyboer.kicherkrabbe.messaging.testing.MessagingTest;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@MessagingTest
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
    void shouldReceiveMessageAndInvokeHandler() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange");
        var routingKey = RoutingKey.parse("test.routing.key");
        var listenerName = "test-listener";

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(1);

        var inboxRepo = new InMemoryMessagingInboxRepo(true);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            receivedMessages.add(message);
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        send(exchange, routingKey, Map.of("key", "value"));

        boolean received = latch.await(10, TimeUnit.SECONDS);

        assertThat(received).isTrue();
        assertThat(receivedMessages).hasSize(1);

        var body = new String(receivedMessages.getFirst().getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains("\"key\"");
        assertThat(body).contains("\"value\"");
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
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
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
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
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
    void shouldReceiveMessageViaTransientListener() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-transient");
        var routingKey = RoutingKey.parse("test.transient.key");
        var listenerName = "test-transient-listener";

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(1);

        var inboxRepo = new InMemoryMessagingInboxRepo(false);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);

        var disposable = factory.createTransientListener(exchange, routingKey, listenerName)
                .doOnNext(message -> {
                    receivedMessages.add(message);
                    latch.countDown();
                })
                .subscribe();

        Thread.sleep(500);

        send(exchange, routingKey, Map.of("transient", "message"));

        boolean received = latch.await(10, TimeUnit.SECONDS);

        disposable.dispose();

        assertThat(received).isTrue();
        assertThat(receivedMessages).hasSize(1);

        var body = new String(receivedMessages.getFirst().getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains("\"transient\"");
        assertThat(body).contains("\"message\"");
    }

    @Test
    void shouldReceiveMultipleMessages() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-multi");
        var routingKey = RoutingKey.parse("test.multi.key");
        var listenerName = "test-multi-listener";

        int messageCount = 5;
        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(messageCount);

        var inboxRepo = new InMemoryMessagingInboxRepo(true);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            receivedMessages.add(message);
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        for (int i = 0; i < messageCount; i++) {
            send(exchange, routingKey, Map.of("index", i));
        }

        boolean allReceived = latch.await(15, TimeUnit.SECONDS);

        assertThat(allReceived).isTrue();
        assertThat(receivedMessages).hasSize(messageCount);

        int inboxSize = 0;
        for (int i = 0; i < 20; i++) {
            var inboxMessages = inboxRepo.findAll().collectList().block();
            inboxSize = inboxMessages.size();
            if (inboxSize == messageCount) {
                break;
            }
            Thread.sleep(100);
        }
        assertThat(inboxSize).isEqualTo(messageCount);
    }

    @Test
    void shouldSupportWildcardRoutingKeys() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-wildcard");
        var listenerRoutingKey = RoutingKey.parse("events.*.created");
        var listenerName = "test-wildcard-listener";

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(2);

        var inboxRepo = new InMemoryMessagingInboxRepo(true);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, listenerRoutingKey, listenerName, message -> {
            receivedMessages.add(message);
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        send(exchange, RoutingKey.parse("events.user.created"), Map.of("type", "user"));
        send(exchange, RoutingKey.parse("events.order.created"), Map.of("type", "order"));

        boolean received = latch.await(10, TimeUnit.SECONDS);

        assertThat(received).isTrue();
        assertThat(receivedMessages).hasSize(2);
    }

    @Test
    void shouldContinueProcessingAfterFailedMessage() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-resilience");
        var routingKey = RoutingKey.parse("test.resilience.key");
        var listenerName = "test-resilience-listener";

        var processedMessages = Collections.synchronizedList(new ArrayList<String>());
        var latch = new CountDownLatch(3);

        var inboxRepo = new InMemoryMessagingInboxRepo(false);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            var body = new String(message.getBody(), StandardCharsets.UTF_8);
            if (body.contains("\"fail\":true")) {
                return Mono.error(new RuntimeException("Simulated failure"));
            }
            processedMessages.add(body);
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        send(exchange, routingKey, Map.of("index", 1, "fail", false));
        send(exchange, routingKey, Map.of("index", 2, "fail", true));
        send(exchange, routingKey, Map.of("index", 3, "fail", false));
        send(exchange, routingKey, Map.of("index", 4, "fail", false));

        boolean allReceived = latch.await(15, TimeUnit.SECONDS);

        assertThat(allReceived).isTrue();
        assertThat(processedMessages).hasSize(3);
        assertThat(processedMessages.stream().anyMatch(m -> m.contains("\"index\":1"))).isTrue();
        assertThat(processedMessages.stream().anyMatch(m -> m.contains("\"index\":3"))).isTrue();
        assertThat(processedMessages.stream().anyMatch(m -> m.contains("\"index\":4"))).isTrue();
        assertThat(processedMessages.stream().noneMatch(m -> m.contains("\"index\":2"))).isTrue();
    }

    @Test
    void shouldRetryTransientErrorsBeforeFailure() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-retry");
        var routingKey = RoutingKey.parse("test.retry.key");
        var listenerName = "test-retry-listener";

        var attemptCount = new AtomicInteger(0);
        var latch = new CountDownLatch(1);
        var failuresBeforeSuccess = 2;

        var inboxRepo = new InMemoryMessagingInboxRepo(false);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
        var listener = factory.createListener(exchange, routingKey, listenerName, message -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt <= failuresBeforeSuccess) {
                return Mono.error(new RuntimeException("Transient failure " + attempt));
            }
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        send(exchange, routingKey, Map.of("test", "retry"));

        boolean received = latch.await(15, TimeUnit.SECONDS);

        assertThat(received).isTrue();
        assertThat(attemptCount.get()).isEqualTo(failuresBeforeSuccess + 1);
    }

    @Test
    void shouldFailAfterMaxRetries() throws InterruptedException {
        var exchange = ExchangeTarget.of("test-exchange-max-retry");
        var routingKey = RoutingKey.parse("test.maxretry.key");
        var listenerName = "test-maxretry-listener";

        var attemptCount = new AtomicInteger(0);

        var inboxRepo = new InMemoryMessagingInboxRepo(false);
        var inbox = new MessagingInbox(inboxRepo, Clock.systemUTC());
        var factory = new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox, containerManager);
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
