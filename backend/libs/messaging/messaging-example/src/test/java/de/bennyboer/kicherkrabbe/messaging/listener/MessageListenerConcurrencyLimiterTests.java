package de.bennyboer.kicherkrabbe.messaging.listener;

import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MessageListenerConcurrencyLimiterTests {

    ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    MessagingInbox inbox = new MessagingInbox(new InMemoryMessagingInboxRepo(false), Clock.systemUTC());

    List<MessageListener> listeners = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (MessageListener listener : listeners) {
            listener.destroy();
        }
        listeners.clear();
    }

    @Test
    void shouldLimitConcurrentMessageProcessing() throws InterruptedException {
        int concurrencyLimit = 2;
        int listenerCount = 5;
        var limiter = new MessageListenerConcurrencyLimiter(concurrencyLimit);
        var maxConcurrent = new AtomicInteger(0);
        var currentConcurrent = new AtomicInteger(0);
        var allProcessedLatch = new CountDownLatch(listenerCount);
        var holdProcessingLatch = new CountDownLatch(1);

        List<Sinks.Many<AcknowledgableMessage>> sinks = new ArrayList<>();

        for (int i = 0; i < listenerCount; i++) {
            Sinks.Many<AcknowledgableMessage> sink = Sinks.many().unicast().onBackpressureBuffer();
            sinks.add(sink);

            var listener = new MessageListener(
                    transactionManager,
                    inbox,
                    sink::asFlux,
                    "concurrency-listener-" + i,
                    message -> Mono.fromCallable(() -> {
                        int concurrent = currentConcurrent.incrementAndGet();
                        maxConcurrent.updateAndGet(current -> Math.max(current, concurrent));

                        holdProcessingLatch.await(10, TimeUnit.SECONDS);

                        currentConcurrent.decrementAndGet();
                        allProcessedLatch.countDown();
                        return null;
                    }),
                    limiter
            );
            listeners.add(listener);
            listener.start();
        }

        Thread.sleep(200);

        for (var sink : sinks) {
            emitAsync(sink);
        }

        Thread.sleep(2000);

        assertThat(maxConcurrent.get()).isLessThanOrEqualTo(concurrencyLimit);
        assertThat(currentConcurrent.get()).isLessThanOrEqualTo(concurrencyLimit);

        holdProcessingLatch.countDown();

        boolean allProcessed = allProcessedLatch.await(15, TimeUnit.SECONDS);
        assertThat(allProcessed).isTrue();
    }

    @Test
    void shouldNackMessageWhenAcquireTimesOut() throws InterruptedException {
        int concurrencyLimit = 1;
        var limiter = new MessageListenerConcurrencyLimiter(concurrencyLimit, Duration.ofSeconds(2));
        var blockingLatch = new CountDownLatch(1);
        var blockingStarted = new CountDownLatch(1);
        var handlerCalled = new AtomicBoolean(false);

        Sinks.Many<AcknowledgableMessage> blockingSink = Sinks.many().unicast().onBackpressureBuffer();
        var blockingListener = new MessageListener(
                transactionManager,
                inbox,
                blockingSink::asFlux,
                "blocking-listener",
                message -> Mono.fromCallable(() -> {
                    blockingStarted.countDown();
                    blockingLatch.await(60, TimeUnit.SECONDS);
                    return null;
                }),
                limiter
        );
        listeners.add(blockingListener);
        blockingListener.start();

        Thread.sleep(200);

        emitAsync(blockingSink);

        boolean started = blockingStarted.await(5, TimeUnit.SECONDS);
        assertThat(started).isTrue();

        Sinks.Many<AcknowledgableMessage> timeoutSink = Sinks.many().unicast().onBackpressureBuffer();
        var timeoutListener = new MessageListener(
                transactionManager,
                inbox,
                timeoutSink::asFlux,
                "timeout-listener",
                message -> {
                    handlerCalled.set(true);
                    return Mono.empty();
                },
                limiter
        );
        listeners.add(timeoutListener);
        timeoutListener.start();

        Thread.sleep(200);

        emitAsync(timeoutSink);

        Thread.sleep(4000);

        assertThat(handlerCalled.get()).isFalse();

        blockingLatch.countDown();
    }

    private void emitAsync(Sinks.Many<AcknowledgableMessage> sink) {
        CompletableFuture.runAsync(() -> emit(sink));
    }

    private void emit(Sinks.Many<AcknowledgableMessage> sink) {
        var properties = new MessageProperties();
        properties.setMessageId(UUID.randomUUID().toString());
        var message = new Message("{}".getBytes(), properties);
        var ackMessage = new NoOpAcknowledgableMessage(message);
        sink.tryEmitNext(ackMessage);
    }

    private record NoOpAcknowledgableMessage(Message message) implements AcknowledgableMessage {

        @Override
        public Message getMessage() {
            return message;
        }

        @Override
        public Mono<Void> ack() {
            return Mono.empty();
        }

        @Override
        public Mono<Void> nack(boolean requeue) {
            return Mono.empty();
        }

        @Override
        public void nackSync(boolean requeue) {
        }

    }

}
