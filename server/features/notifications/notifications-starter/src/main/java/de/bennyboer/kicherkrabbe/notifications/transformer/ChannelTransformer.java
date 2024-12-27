package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.ChannelDTO;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;

public class ChannelTransformer {

    public static ChannelDTO toApi(Channel channel) {
        var result = new ChannelDTO();

        result.type = ChannelTypeTransformer.toApi(channel.getType());
        result.mail = channel.getMail()
                .map(EMail::getValue)
                .orElse(null);
        result.telegram = channel.getTelegram()
                .map(TelegramTransformer::toApi)
                .orElse(null);

        return result;
    }

    public static Channel toInternal(ChannelDTO channel) {
        var type = ChannelTypeTransformer.toInternal(channel.type);

        return switch (type) {
            case EMAIL -> Channel.mail(EMail.of(channel.mail));
            case TELEGRAM -> Channel.telegram(TelegramTransformer.toInternal(channel.telegram));
            case UNKNOWN -> throw new IllegalArgumentException("Unknown channel type: " + channel.type);
        };
    }

}
