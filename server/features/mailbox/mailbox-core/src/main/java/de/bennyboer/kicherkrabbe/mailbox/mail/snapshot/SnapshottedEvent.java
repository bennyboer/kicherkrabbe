package de.bennyboer.kicherkrabbe.mailbox.mail.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailbox.mail.Content;
import de.bennyboer.kicherkrabbe.mailbox.mail.Origin;
import de.bennyboer.kicherkrabbe.mailbox.mail.Sender;
import de.bennyboer.kicherkrabbe.mailbox.mail.Subject;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    Origin origin;

    Sender sender;

    Subject subject;

    Content content;

    Instant receivedAt;

    @Nullable
    Instant readAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            Origin origin,
            Sender sender,
            Subject subject,
            Content content,
            Instant receivedAt,
            @Nullable Instant readAt,
            @Nullable Instant deletedAt
    ) {
        notNull(origin, "Origin must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(content, "Content must be given");
        notNull(receivedAt, "Received at must be given");

        return new SnapshottedEvent(
                origin,
                sender,
                subject,
                content,
                receivedAt,
                readAt,
                deletedAt
        );
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

}
