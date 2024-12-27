package de.bennyboer.kicherkrabbe.notifications.settings.system.channels.deactivate;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DeactivateSystemChannelCmd implements Command {

    ChannelType channelType;

    public static DeactivateSystemChannelCmd of(ChannelType channelType) {
        notNull(channelType, "Channel type must be given");

        return new DeactivateSystemChannelCmd(channelType);
    }

}
