package de.bennyboer.kicherkrabbe.inquiries.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.inquiries.Message;
import de.bennyboer.kicherkrabbe.inquiries.Sender;
import de.bennyboer.kicherkrabbe.inquiries.Subject;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    Sender sender;

    Subject subject;

    Message message;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            Sender sender,
            Subject subject,
            Message message,
            Instant createdAt,
            @Nullable Instant deletedAt
    ) {
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(message, "Message must be given");
        notNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(sender, subject, message, createdAt, deletedAt);
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

}
