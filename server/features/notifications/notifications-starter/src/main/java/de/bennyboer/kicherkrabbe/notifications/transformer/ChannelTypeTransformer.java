package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.ChannelTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;

public class ChannelTypeTransformer {

    public static ChannelTypeDTO toApi(ChannelType type) {
        return switch (type) {
            case EMAIL -> ChannelTypeDTO.EMAIL;
            case TELEGRAM -> ChannelTypeDTO.TELEGRAM;
            case UNKNOWN -> throw new IllegalArgumentException("Unknown channel type");
        };
    }

    public static ChannelType toInternal(ChannelTypeDTO type) {
        return switch (type) {
            case EMAIL -> ChannelType.EMAIL;
            case TELEGRAM -> ChannelType.TELEGRAM;
        };
    }

}
