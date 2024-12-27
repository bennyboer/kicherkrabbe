package de.bennyboer.kicherkrabbe.notifications.notification.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.notifications.notification.Message;
import de.bennyboer.kicherkrabbe.notifications.notification.Origin;
import de.bennyboer.kicherkrabbe.notifications.notification.Target;
import de.bennyboer.kicherkrabbe.notifications.notification.Title;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    Origin origin;

    Target target;

    Set<Channel> channels;

    Title title;

    Message message;

    Instant sentAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            Origin origin,
            Target target,
            Set<Channel> channels,
            Title title,
            Message message,
            Instant sentAt,
            @Nullable Instant deletedAt
    ) {
        notNull(origin, "Origin must be given");
        notNull(target, "Target must be given");
        notNull(channels, "Channels must be given");
        notNull(title, "Title must be given");
        notNull(message, "Message must be given");
        notNull(sentAt, "SentAt must be given");

        return new SnapshottedEvent(
                origin,
                target,
                channels,
                title,
                message,
                sentAt,
                deletedAt
        );
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
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
