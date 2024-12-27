package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import lombok.Getter;

@Getter
public class ChannelAlreadyDeactivatedException extends RuntimeException {

    private final ChannelType channelType;

    public ChannelAlreadyDeactivatedException(ChannelType channelType) {
        super("Channel '%s' is already deactivated.".formatted(channelType));

        this.channelType = channelType;
    }

}
