package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.MessagingOutboxEntryPublisher;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import reactor.util.retry.Retry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class MessagingOutboxChangeStreamTests {

    private static final Retry FAST_RETRY = Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(10))
            .maxBackoff(Duration.ofMillis(100));

    private final MessageTarget target = MessageTarget.exchange(ExchangeTarget.of("test-exchange"));

    private final RoutingKey routingKey = RoutingKey.parse("test.key");

    private MessagingOutboxChangeStream changeStream;

    @AfterEach
    void tearDown() {
        if (changeStream != null) {
            changeStream.destroy();
        }
    }

    @Test
    void shouldReconnectAfterWatchInsertsFailure() throws InterruptedException {
        var subscriptionCount = new AtomicInteger(0);
        var successLatch = new CountDownLatch(1);

        var repo = new FakeMessagingOutboxRepo() {
            @Override
            public Flux<MessagingOutboxEntry> watchInserts() {
                int attempt = subscriptionCount.incrementAndGet();
                if (attempt <= 2) {
                    return Flux.error(new RuntimeException("Simulated connection failure #" + attempt));
                }
                successLatch.countDown();
                return Flux.never();
            }
        };

        var outbox = new MessagingOutbox(repo, new NoOpPublisher(), 10, Clock.systemUTC());
        changeStream = new MessagingOutboxChangeStream(repo, outbox, FAST_RETRY);

        changeStream.start();

        boolean completed = successLatch.await(5, TimeUnit.SECONDS);

        assertThat(completed)
                .describedAs("Expected change stream to reconnect after failures, but it didn't within timeout")
                .isTrue();
        assertThat(subscriptionCount.get()).isEqualTo(3);
    }

    @Test
    void shouldReconnectAfterLockingFailure() throws InterruptedException {
        var lockAttempts = new AtomicInteger(0);
        var successLatch = new CountDownLatch(1);

        var repo = new FakeMessagingOutboxRepo() {
            @Override
            public Flux<MessagingOutboxEntry> watchInserts() {
                var entry = MessagingOutboxEntry.create(target, routingKey, Map.of(), Clock.systemUTC());
                return Flux.just(entry);
            }

            @Override
            public Mono<Void> lockNextPublishableEntries(MessagingOutboxEntryLock lock, int maxEntries, Clock clock) {
                int attempt = lockAttempts.incrementAndGet();
                if (attempt <= 2) {
                    return Mono.error(new RuntimeException("Simulated DB failure #" + attempt));
                }
                successLatch.countDown();
                return Mono.empty();
            }
        };

        var outbox = new MessagingOutbox(repo, new NoOpPublisher(), 10, Clock.systemUTC());
        changeStream = new MessagingOutboxChangeStream(repo, outbox, FAST_RETRY);

        changeStream.start();

        boolean completed = successLatch.await(5, TimeUnit.SECONDS);

        assertThat(completed)
                .describedAs("Expected change stream to retry after locking failures")
                .isTrue();
        assertThat(lockAttempts.get()).isEqualTo(3);
    }

    @Test
    void shouldStopListeningOnDestroy() throws InterruptedException {
        var subscribed = new CountDownLatch(1);
        var cancelled = new CountDownLatch(1);

        var repo = new FakeMessagingOutboxRepo() {
            @Override
            public Flux<MessagingOutboxEntry> watchInserts() {
                return Flux.<MessagingOutboxEntry>never()
                        .doOnSubscribe(s -> subscribed.countDown())
                        .doOnCancel(cancelled::countDown);
            }
        };

        var outbox = new MessagingOutbox(repo, new NoOpPublisher(), 10, Clock.systemUTC());
        changeStream = new MessagingOutboxChangeStream(repo, outbox);

        changeStream.start();
        subscribed.await(5, TimeUnit.SECONDS);

        changeStream.destroy();

        boolean wasCancelled = cancelled.await(5, TimeUnit.SECONDS);
        assertThat(wasCancelled).isTrue();
    }

    @Test
    void shouldTriggerPublishOnEachInsert() throws InterruptedException {
        var insertSink = Sinks.many().multicast().<MessagingOutboxEntry>directBestEffort();
        var publishCount = new AtomicInteger(0);
        var expectedPublishes = 3;
        var allPublished = new CountDownLatch(expectedPublishes);

        var repo = new FakeMessagingOutboxRepo() {
            @Override
            public Flux<MessagingOutboxEntry> watchInserts() {
                return insertSink.asFlux();
            }
        };

        var countingPublisher = new MessagingOutboxEntryPublisher() {
            @Override
            public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
                publishCount.incrementAndGet();
                allPublished.countDown();
                return Mono.empty();
            }
        };

        var outbox = new MessagingOutbox(repo, countingPublisher, 10, Clock.systemUTC());
        changeStream = new MessagingOutboxChangeStream(repo, outbox);

        changeStream.start();
        Thread.sleep(100);

        for (int i = 0; i < expectedPublishes; i++) {
            var entry = MessagingOutboxEntry.create(target, routingKey, Map.of("i", i), Clock.systemUTC());
            insertSink.tryEmitNext(entry);
            Thread.sleep(50);
        }

        boolean completed = allPublished.await(10, TimeUnit.SECONDS);

        assertThat(completed).isTrue();
        assertThat(publishCount.get()).isEqualTo(expectedPublishes);
    }

    @Test
    void shouldContinueProcessingAfterPublishErrorIsHandledByOutbox() throws InterruptedException {
        var insertSink = Sinks.many().multicast().<MessagingOutboxEntry>directBestEffort();
        var publishAttempts = new AtomicInteger(0);
        var allAttempts = new CountDownLatch(3);

        var repo = new FakeMessagingOutboxRepo() {
            @Override
            public Flux<MessagingOutboxEntry> watchInserts() {
                return insertSink.asFlux();
            }
        };

        var sometimesFailingPublisher = new MessagingOutboxEntryPublisher() {
            @Override
            public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
                int attempt = publishAttempts.incrementAndGet();
                allAttempts.countDown();
                if (attempt == 2) {
                    return Mono.error(new RuntimeException("Temporary broker failure"));
                }
                return Mono.empty();
            }
        };

        var outbox = new MessagingOutbox(repo, sometimesFailingPublisher, 10, Clock.systemUTC());
        changeStream = new MessagingOutboxChangeStream(repo, outbox);

        changeStream.start();
        Thread.sleep(100);

        for (int i = 1; i <= 3; i++) {
            var entry = MessagingOutboxEntry.create(target, routingKey, Map.of("n", i), Clock.systemUTC());
            insertSink.tryEmitNext(entry);
            Thread.sleep(100);
        }

        boolean completed = allAttempts.await(10, TimeUnit.SECONDS);

        assertThat(completed)
                .describedAs("Stream should continue processing after outbox handles publish error")
                .isTrue();
        assertThat(publishAttempts.get()).isEqualTo(3);
    }

    private static class NoOpPublisher implements MessagingOutboxEntryPublisher {
        @Override
        public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
            return Mono.empty();
        }
    }

    private static class FakeMessagingOutboxRepo implements MessagingOutboxRepo {
        @Override
        public Mono<Void> save(Collection<MessagingOutboxEntry> entries) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> insert(Collection<MessagingOutboxEntry> entries) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> lockNextPublishableEntries(MessagingOutboxEntryLock lock, int maxEntries, Clock clock) {
            return Mono.empty();
        }

        @Override
        public Flux<MessagingOutboxEntry> findLockedEntries(MessagingOutboxEntryLock lock) {
            return Flux.empty();
        }

        @Override
        public Mono<Void> unlockEntriesOlderThan(Instant date) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> removeAcknowledgedEntriesOlderThan(Instant date) {
            return Mono.empty();
        }

        @Override
        public Flux<MessagingOutboxEntry> findFailedEntriesOlderThan(Instant date) {
            return Flux.empty();
        }

        @Override
        public Flux<MessagingOutboxEntry> watchInserts() {
            return Flux.never();
        }
    }

}
