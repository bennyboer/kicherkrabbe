package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import lombok.Getter;

@Getter
public class ChannelAlreadyActivatedException extends RuntimeException {

    private final ChannelType channelType;

    public ChannelAlreadyActivatedException(ChannelType channelType) {
        super("Channel '%s' is already activated.".formatted(channelType));

        this.channelType = channelType;
    }

}
