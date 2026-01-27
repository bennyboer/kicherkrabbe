package de.bennyboer.kicherkrabbe.notifications.notification.send;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.notifications.notification.Message;
import de.bennyboer.kicherkrabbe.notifications.notification.Origin;
import de.bennyboer.kicherkrabbe.notifications.notification.Target;
import de.bennyboer.kicherkrabbe.notifications.notification.Title;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SentEvent implements Event {

    public static final EventName NAME = EventName.of("SENT");

    public static final Version VERSION = Version.zero();

    Origin origin;

    Target target;

    Set<Channel> channels;

    Title title;

    Message message;

    public static SentEvent of(
            Origin origin,
            Target target,
            Set<Channel> channels,
            Title title,
            Message message
    ) {
        notNull(origin, "Origin must be given");
        notNull(target, "Target must be given");
        notNull(channels, "Channels must be given");
        notNull(title, "Title must be given");
        notNull(message, "Message must be given");

        return new SentEvent(
                origin,
                target,
                channels,
                title,
                message
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

}
