package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import lombok.Getter;

@Getter
public class ChannelUnavailableException extends RuntimeException {

    private final ChannelType channelType;

    public ChannelUnavailableException(ChannelType channelType) {
        super("Channel '%s' is currently unavailable.".formatted(channelType));

        this.channelType = channelType;
    }

}
