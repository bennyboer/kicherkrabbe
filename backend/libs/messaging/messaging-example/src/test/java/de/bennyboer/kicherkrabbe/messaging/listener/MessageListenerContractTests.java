package de.bennyboer.kicherkrabbe.messaging.listener;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

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

abstract class MessageListenerContractTests {

    @Autowired
    MessageListenerFactory factory;

    @Autowired
    MessagingOutbox outbox;

    @Autowired
    ReactiveTransactionManager transactionManager;

    List<MessageListener> listeners = new ArrayList<>();

    List<Disposable> disposables = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (MessageListener listener : listeners) {
            listener.destroy();
        }
        listeners.clear();

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
    }

    @Test
    void shouldReceiveMessageAndInvokeHandler() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange");
        var routingKey = RoutingKey.parse("test.routing.key");

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(1);

        var listener = factory.createListener(exchange, routingKey, "contract-listener", message -> {
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
    void shouldReceiveMultipleMessages() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange-multi");
        var routingKey = RoutingKey.parse("test.multi.key");
        int messageCount = 5;

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(messageCount);

        var listener = factory.createListener(exchange, routingKey, "contract-multi-listener", message -> {
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
    }

    @Test
    void shouldSupportWildcardRoutingKeys() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange-wildcard");
        var listenerRoutingKey = RoutingKey.parse("events.*.created");

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(2);

        var listener = factory.createListener(exchange, listenerRoutingKey, "contract-wildcard-listener", message -> {
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
    void shouldSupportHashWildcardRoutingKeys() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange-hash-wildcard");
        var listenerRoutingKey = RoutingKey.parse("events.#");

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(3);

        var listener = factory.createListener(exchange, listenerRoutingKey, "contract-hash-wildcard-listener", message -> {
            receivedMessages.add(message);
            latch.countDown();
            return Mono.empty();
        });
        listeners.add(listener);
        listener.start();

        send(exchange, RoutingKey.parse("events.user.created"), Map.of("type", "user-created"));
        send(exchange, RoutingKey.parse("events.order.item.added"), Map.of("type", "order-item-added"));
        send(exchange, RoutingKey.parse("events.deleted"), Map.of("type", "deleted"));

        boolean received = latch.await(10, TimeUnit.SECONDS);

        assertThat(received).isTrue();
        assertThat(receivedMessages).hasSize(3);
    }

    @Test
    void shouldReceiveMessageViaTransientListener() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange-transient");
        var routingKey = RoutingKey.parse("test.transient.key");

        var receivedMessages = Collections.synchronizedList(new ArrayList<Message>());
        var latch = new CountDownLatch(1);

        var disposable = factory.createTransientListener(exchange, routingKey, "contract-transient-listener")
                .doOnNext(message -> {
                    receivedMessages.add(message);
                    latch.countDown();
                })
                .subscribe();
        disposables.add(disposable);

        Thread.sleep(500);

        send(exchange, routingKey, Map.of("transient", "message"));

        boolean received = latch.await(10, TimeUnit.SECONDS);

        assertThat(received).isTrue();
        assertThat(receivedMessages).hasSize(1);

        var body = new String(receivedMessages.getFirst().getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains("\"transient\"");
        assertThat(body).contains("\"message\"");
    }

    @Test
    void shouldRetryTransientErrorsBeforeSuccess() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange-retry");
        var routingKey = RoutingKey.parse("test.retry.key");

        var attemptCount = new AtomicInteger(0);
        var latch = new CountDownLatch(1);
        var failuresBeforeSuccess = 2;

        var listener = factory.createListener(exchange, routingKey, "contract-retry-listener", message -> {
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
    void shouldContinueProcessingAfterFailedMessage() throws InterruptedException {
        var exchange = ExchangeTarget.of("contract-test-exchange-resilience");
        var routingKey = RoutingKey.parse("test.resilience.key");

        var processedMessages = Collections.synchronizedList(new ArrayList<String>());
        var latch = new CountDownLatch(3);

        var listener = factory.createListener(exchange, routingKey, "contract-resilience-listener", message -> {
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
