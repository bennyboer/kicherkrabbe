package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.notification.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupNotification {

    NotificationId id;

    Version version;

    Origin origin;

    Target target;

    Set<Channel> channels;

    Title title;

    Message message;

    Instant sentAt;

    public static LookupNotification of(
            NotificationId id,
            Version version,
            Origin origin,
            Target target,
            Set<Channel> channels,
            Title title,
            Message message,
            Instant sentAt
    ) {
        notNull(id, "ID must be given");
        notNull(version, "Version must be given");
        notNull(origin, "Origin must be given");
        notNull(target, "Target must be given");
        notNull(channels, "Channels must be given");
        notNull(title, "Title must be given");
        notNull(message, "Message must be given");
        notNull(sentAt, "Sent at must be given");

        return new LookupNotification(id, version, origin, target, channels, title, message, sentAt);
    }

}
