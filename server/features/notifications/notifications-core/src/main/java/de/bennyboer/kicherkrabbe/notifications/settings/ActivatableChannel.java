package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class ActivatableChannel {

    Channel channel;

    boolean active;

    public static ActivatableChannel of(Channel channel, boolean active) {
        notNull(channel, "Channel must be given");

        return new ActivatableChannel(channel, active);
    }

    public ActivatableChannel activate() {
        return withActive(true);
    }

    public ActivatableChannel deactivate() {
        return withActive(false);
    }

    public boolean isInactive() {
        return !isActive();
    }

    @Override
    public String toString() {
        return "ActivatableChannel(channel=%s, active=%s)".formatted(channel, active);
    }

}
