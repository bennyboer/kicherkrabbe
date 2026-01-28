package de.bennyboer.kicherkrabbe.notifications.settings.system.channels.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateSystemChannelCmd implements Command {

    Channel channel;

    public static UpdateSystemChannelCmd of(Channel channel) {
        notNull(channel, "Channel must be given");

        return new UpdateSystemChannelCmd(channel);
    }

}
