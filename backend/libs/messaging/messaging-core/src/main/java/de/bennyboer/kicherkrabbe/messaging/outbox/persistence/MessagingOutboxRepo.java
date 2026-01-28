package de.bennyboer.kicherkrabbe.messaging.outbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryLock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;

public interface MessagingOutboxRepo {

    /**
     * Save the given entry. If the entry already exists, it will be updated.
     * For example, you might want to update entries to have been published and unlocked.
     */
    Mono<Void> save(Collection<MessagingOutboxEntry> entries);

    /**
     * Insert new entries.
     */
    Mono<Void> insert(Collection<MessagingOutboxEntry> entries);

    /**
     * Lock the next n publishable entries with the given lock. This is used to ensure that we don't publish the same
     * entry multiple times. Use in connection with {@link #findLockedEntries(MessagingOutboxEntryLock)}  to find the
     * locked entries (for example for publishing and unlocking them).
     */
    Mono<Void> lockNextPublishableEntries(MessagingOutboxEntryLock lock, int maxEntries, Clock clock);

    Flux<MessagingOutboxEntry> findLockedEntries(MessagingOutboxEntryLock lock);

    /**
     * For some reason entries might have been locked (e. g. the service crashed). This method should be called
     * periodically to unlock entries that have been locked for too long.
     */
    Mono<Void> unlockEntriesOlderThan(Instant date);

    /**
     * We don't want the repository to grow indefinitely. This method should be called periodically to remove old
     * entries
     * that have been acknowledged.
     */
    Mono<Void> removeAcknowledgedEntriesOlderThan(Instant date);

    Flux<MessagingOutboxEntry> findFailedEntriesOlderThan(Instant date);
    
    Flux<MessagingOutboxEntry> watchInserts();

}
