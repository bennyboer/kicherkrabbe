package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class MessagingOutboxEntry {

    private final static int MAX_RETRY_COUNT = 5;

    MessagingOutboxEntryId id;

    MessageTarget target;

    RoutingKey routingKey;

    Map<String, Object> payload;

    Instant createdAt;

    /**
     * We lock entries before we publish them to the messaging system. This is to ensure that we don't publish the same
     * entry multiple times. See also the lock attribute.
     */
    @Nullable
    Instant lockedAt;

    /**
     * A lock is set when we try to publish an entry. This is to ensure that we don't publish the same entry multiple
     * times.
     */
    @Nullable
    MessagingOutboxEntryLock lock;

    /**
     * Once an entry is acknowledged, it is considered to be successfully processed and can be safely removed from
     * the outbox.
     */
    @Nullable
    Instant acknowledgedAt;

    /**
     * Once an entry is marked as failed, it is considered to be failed and can be retried.
     */
    @Nullable
    Instant failedAt;

    /**
     * How often the entry has been retried by now.
     */
    int retryCount;

    public static MessagingOutboxEntry of(
            MessagingOutboxEntryId id,
            MessageTarget target,
            RoutingKey routingKey,
            Map<String, Object> payload,
            Instant createdAt,
            Instant lockedAt,
            MessagingOutboxEntryLock lock,
            Instant acknowledgedAt,
            Instant failedAt,
            int retryCount
    ) {
        notNull(id, "Id must be given");
        notNull(target, "Target must be given");
        notNull(routingKey, "Routing key must be given");
        notNull(payload, "Payload must be given");
        notNull(createdAt, "Created at must be given");

        return new MessagingOutboxEntry(
                id,
                target,
                routingKey,
                payload,
                createdAt,
                lockedAt,
                lock,
                acknowledgedAt,
                failedAt,
                retryCount
        );
    }

    public static MessagingOutboxEntry create(
            MessageTarget target,
            RoutingKey routingKey,
            Map<String, Object> payload,
            Clock clock
    ) {
        Instant createdAt = clock.instant();

        var id = MessagingOutboxEntryId.create();

        return of(
                id,
                target,
                routingKey,
                payload,
                createdAt,
                null,
                null,
                null,
                null,
                0
        );
    }

    public MessagingOutboxEntry acknowledge(Clock clock) {
        check(getAcknowledgedAt().isEmpty(), "Entry must not be acknowledged yet");

        return withAcknowledgedAt(clock.instant());
    }

    public MessagingOutboxEntry lock(MessagingOutboxEntryLock lock, Clock clock) {
        return withLock(lock)
                .withLockedAt(clock.instant());
    }

    public MessagingOutboxEntry unlock() {
        return withLock(null)
                .withLockedAt(null);
    }

    public MessagingOutboxEntry failed(Clock clock) {
        if (retryCount >= MAX_RETRY_COUNT) {
            return withFailedAt(clock.instant());
        }

        return unlock()
                .withRetryCount(retryCount + 1);
    }

    public Optional<Instant> getLockedAt() {
        return Optional.ofNullable(lockedAt);
    }

    public Optional<MessagingOutboxEntryLock> getLock() {
        return Optional.ofNullable(lock);
    }

    public Optional<Instant> getFailedAt() {
        return Optional.ofNullable(failedAt);
    }

    public Optional<Instant> getAcknowledgedAt() {
        return Optional.ofNullable(acknowledgedAt);
    }

    public boolean isLocked() {
        return getLockedAt().isPresent();
    }

}
