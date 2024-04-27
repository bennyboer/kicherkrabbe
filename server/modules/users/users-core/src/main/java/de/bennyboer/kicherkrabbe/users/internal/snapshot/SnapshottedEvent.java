package de.bennyboer.kicherkrabbe.users.internal.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.users.internal.FullName;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
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

    FullName name;

    Mail mail;

    Instant createdAt;

    @Nullable
    @Getter(NONE)
    Instant deletedAt;

    public static SnapshottedEvent of(FullName name, Mail mail, Instant createdAt, @Nullable Instant deletedAt) {
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");
        notNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(name, mail, createdAt, deletedAt);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    @Override
    public boolean isSnapshot() {
        return true;
    }
    
}
