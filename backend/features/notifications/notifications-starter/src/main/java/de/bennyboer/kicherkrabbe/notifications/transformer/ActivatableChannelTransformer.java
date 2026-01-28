package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.ActivatableChannelDTO;
import de.bennyboer.kicherkrabbe.notifications.settings.ActivatableChannel;

import java.util.Set;
import java.util.stream.Collectors;

public class ActivatableChannelTransformer {

    public static Set<ActivatableChannelDTO> toApi(Set<ActivatableChannel> channels) {
        return channels.stream()
                .map(ActivatableChannelTransformer::toApi)
                .collect(Collectors.toSet());
    }

    public static ActivatableChannelDTO toApi(ActivatableChannel channel) {
        var result = new ActivatableChannelDTO();

        result.active = channel.isActive();
        result.channel = ChannelTransformer.toApi(channel.getChannel());

        return result;
    }

}
