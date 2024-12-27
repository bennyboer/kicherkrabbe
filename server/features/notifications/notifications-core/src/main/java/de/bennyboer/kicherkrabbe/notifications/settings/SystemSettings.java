package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.HashSet;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class SystemSettings {

    boolean enabled;

    Set<ActivatableChannel> channels;

    public static SystemSettings of(boolean enabled, Set<ActivatableChannel> channels) {
        notNull(channels, "Channels must be given");

        return new SystemSettings(enabled, channels);
    }

    public static SystemSettings init() {
        return of(false, Set.of());
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public SystemSettings enable() {
        if (isEnabled()) {
            throw new SystemNotificationsAlreadyEnabledException();
        }

        return withEnabled(true);
    }

    public SystemSettings disable() {
        if (isDisabled()) {
            throw new SystemNotificationsAlreadyDisabledException();
        }

        return withEnabled(false);
    }

    public SystemSettings updateChannel(Channel channel) {
        Set<ActivatableChannel> updatedChannels = new HashSet<>(channels);

        boolean isActive = updatedChannels.stream()
                .filter(c -> c.getChannel().getType().equals(channel.getType()))
                .map(ActivatableChannel::isActive)
                .findFirst()
                .orElse(false);
        updatedChannels.removeIf(c -> c.getChannel().getType().equals(channel.getType()));
        updatedChannels.add(ActivatableChannel.of(channel, isActive));

        return withChannels(updatedChannels);
    }

    public SystemSettings activateChannel(ChannelType channelType) {
        Set<ActivatableChannel> updatedChannels = new HashSet<>(channels);

        ActivatableChannel channelToActivate = updatedChannels.stream()
                .filter(c -> c.getChannel().getType().equals(channelType))
                .findFirst()
                .orElseThrow(() -> new ChannelUnavailableException(channelType));

        if (channelToActivate.isActive()) {
            throw new ChannelAlreadyActivatedException(channelType);
        }

        updatedChannels.remove(channelToActivate);
        updatedChannels.add(channelToActivate.activate());

        return withChannels(updatedChannels);
    }

    public SystemSettings deactivateChannel(ChannelType channelType) {
        Set<ActivatableChannel> updatedChannels = new HashSet<>(channels);

        ActivatableChannel channelToDeactivate = updatedChannels.stream()
                .filter(c -> c.getChannel().getType().equals(channelType))
                .findFirst()
                .orElseThrow(() -> new ChannelUnavailableException(channelType));

        if (channelToDeactivate.isInactive()) {
            throw new ChannelAlreadyDeactivatedException(channelType);
        }

        updatedChannels.remove(channelToDeactivate);
        updatedChannels.add(channelToDeactivate.deactivate());

        return withChannels(updatedChannels);
    }

}
