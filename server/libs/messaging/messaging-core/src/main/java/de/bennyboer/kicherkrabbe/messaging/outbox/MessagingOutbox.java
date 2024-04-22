package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.MessagingOutboxEntryPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.reactive.TransactionContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class MessagingOutbox {

    private static final Duration OLD_FAILED_ENTRIES_DURATION = Duration.ofMinutes(30);

    private static final Duration STALE_LOCKED_ENTRIES_DURATION = Duration.ofMinutes(5);

    private static final Duration ACKNOWLEDGED_ENTRIES_KEEP_DURATION = Duration.ofDays(30);

    private final MessagingOutboxRepo repo;

    private final MessagingOutboxEntryPublisher publisher;

    private final int batchSize;

    private final Clock clock;

    public Mono<Void> insert(MessagingOutboxEntry... entries) {
        return insert(List.of(entries));
    }

    public Mono<Void> insert(Collection<MessagingOutboxEntry> entries) {
        return assertThatWeAreInATransaction()
                .then(repo.insert(entries))
                .then(publishNextUnpublishedEntries());
    }

    public Mono<Void> publishNextUnpublishedEntries() {
        var lock = MessagingOutboxEntryLock.create();

        return repo.lockNextPublishableEntries(lock, batchSize, clock)
                .thenMany(repo.findLockedEntries(lock))
                .collectList()
                .flatMap(entries -> publisher.publishAll(entries)
                        .then(acknowledgeEntries(entries))
                        .doOnSuccess(ignored -> log.debug("Published {} entries.", entries.size()))
                        .onErrorResume(e -> {
                            log.warn("Failed to publish entries. Unlocking entries.", e);
                            return resetEntriesAfterPublishingFailed(entries).then(Mono.empty());
                        }));
    }

    public Flux<MessagingOutboxEntry> findStaleFailedEntries() {
        return repo.findFailedEntriesOlderThan(clock.instant().minus(OLD_FAILED_ENTRIES_DURATION));
    }

    public Mono<Void> unlockStaleEntries() {
        return repo.unlockEntriesOlderThan(clock.instant().minus(STALE_LOCKED_ENTRIES_DURATION));
    }

    public Mono<Void> cleanupOldAcknowledgedEntries() {
        return repo.removeAcknowledgedEntriesOlderThan(clock.instant().minus(ACKNOWLEDGED_ENTRIES_KEEP_DURATION));
    }

    private Mono<Void> acknowledgeEntries(List<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .map(entry -> entry.acknowledge(clock))
                .collectList()
                .flatMap(repo::save);
    }

    private Mono<Void> resetEntriesAfterPublishingFailed(List<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .map(entry -> entry.failed(clock))
                .collectList()
                .flatMap(repo::save);
    }

    private Mono<Void> assertThatWeAreInATransaction() {
        return Mono.deferContextual(context -> {
            boolean isInTransaction = context.getOrEmpty(TransactionContext.class).isPresent();

            if (!isInTransaction) {
                return Mono.error(new IllegalStateException(
                        "No transaction active. A transaction is required to insert entries into the outbox."
                ));
            }

            return Mono.empty();
        });
    }

}
