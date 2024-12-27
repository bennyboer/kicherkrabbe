package de.bennyboer.kicherkrabbe.notifications.settings.system.channels.activate;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SystemChannelActivatedEvent implements Event {

    public static final EventName NAME = EventName.of("SYSTEM_CHANNEL_ACTIVATED");

    public static final Version VERSION = Version.zero();

    ChannelType channelType;

    public static SystemChannelActivatedEvent of(ChannelType channelType) {
        notNull(channelType, "Channel type must be given");

        return new SystemChannelActivatedEvent(channelType);
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
