package de.bennyboer.kicherkrabbe.auth.internal.credentials.events;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.Name;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.UserId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.password.EncodedPassword;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    Name name;

    EncodedPassword encodedPassword;

    UserId userId;

    int failedUsageAttempts;

    @Nullable
    @Getter(NONE)
    Instant lastUsedAt;

    @Nullable
    @Getter(NONE)
    Instant deletedAt;

    public static SnapshottedEvent of(
            Name name,
            EncodedPassword encodedPassword,
            UserId userId,
            int failedUsageAttempts,
            @Nullable Instant lastUsedAt,
            @Nullable Instant deletedAt
    ) {
        notNull(name, "Name must be given");
        notNull(encodedPassword, "Encoded password must be given");
        notNull(userId, "User ID must be given");

        return new SnapshottedEvent(name, encodedPassword, userId, failedUsageAttempts, lastUsedAt, deletedAt);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isSnapshot() {
        return true;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public Optional<Instant> getLastUsedAt() {
        return Optional.ofNullable(lastUsedAt);
    }

}
