package de.bennyboer.kicherkrabbe.notifications.settings.system.channels.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SystemChannelUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("SYSTEM_CHANNEL_UPDATED");

    public static final Version VERSION = Version.zero();

    Channel channel;

    public static SystemChannelUpdatedEvent of(Channel channel) {
        notNull(channel, "Channel must be given");

        return new SystemChannelUpdatedEvent(channel);
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
