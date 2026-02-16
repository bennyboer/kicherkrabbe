package de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryId;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryLock;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessagingOutboxRepo implements MessagingOutboxRepo {

    private final Map<MessagingOutboxEntryId, MessagingOutboxEntry> entries = new ConcurrentHashMap<>();

    private final Sinks.Many<MessagingOutboxEntry> insertSink = Sinks.many().multicast().directBestEffort();

    @Override
    public Mono<Void> save(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .doOnNext(entry -> {
                    this.entries.put(entry.getId(), entry);
                    insertSink.tryEmitNext(entry);
                })
                .then();
    }

    @Override
    public Mono<Void> insert(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .flatMap(entry -> {
                    if (this.entries.containsKey(entry.getId())) {
                        return Mono.error(new DuplicateKeyException("Entry with ID %s already exists".formatted(entry.getId())));
                    }

                    return Mono.just(entry);
                })
                .doOnNext(entry -> {
                    this.entries.put(entry.getId(), entry);
                    insertSink.tryEmitNext(entry);
                })
                .then();
    }

    @Override
    public synchronized Mono<Void> lockNextPublishableEntries(MessagingOutboxEntryLock lock, int maxEntries, Clock clock) {
        var publishable = new ArrayList<>(entries.values()).stream()
                .filter(entry -> entry.getLockedAt().isEmpty()
                        && entry.getFailedAt().isEmpty()
                        && entry.getAcknowledgedAt().isEmpty())
                .sorted(Comparator.comparing(MessagingOutboxEntry::getCreatedAt))
                .limit(maxEntries)
                .toList();

        for (var entry : publishable) {
            this.entries.put(entry.getId(), entry.lock(lock, clock));
        }

        return Mono.empty();
    }

    @Override
    public Flux<MessagingOutboxEntry> findLockedEntries(MessagingOutboxEntryLock lock) {
        return findAll()
                .filter(entry -> entry.getLock().equals(Optional.of(lock)))
                .sort(Comparator.comparing(MessagingOutboxEntry::getCreatedAt));
    }

    @Override
    public Mono<Void> unlockEntriesOlderThan(Instant date) {
        return findAll()
                .filter(entry -> entry.getLockedAt().map(lockedAt -> lockedAt.isBefore(date)).orElse(false))
                .map(MessagingOutboxEntry::unlock)
                .doOnNext(entry -> this.entries.put(entry.getId(), entry))
                .then();
    }

    @Override
    public Mono<Void> removeAcknowledgedEntriesOlderThan(Instant date) {
        return findAll()
                .filter(entry -> entry.getAcknowledgedAt()
                        .map(acknowledgedAt -> acknowledgedAt.isBefore(date))
                        .orElse(false))
                .doOnNext(entry -> this.entries.remove(entry.getId()))
                .then();
    }

    @Override
    public Flux<MessagingOutboxEntry> findFailedEntriesOlderThan(Instant date) {
        return findAll()
                .filter(entry -> entry.getFailedAt().map(failedAt -> failedAt.isBefore(date)).orElse(false));
    }

    @Override
    public Flux<MessagingOutboxEntry> watchInserts() {
        return insertSink.asFlux();
    }

    public Flux<MessagingOutboxEntry> findAll() {
        return Flux.defer(() -> Flux.fromIterable(new ArrayList<>(entries.values())));
    }

}
