package de.bennyboer.kicherkrabbe.notifications.notification.send;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.notifications.notification.Message;
import de.bennyboer.kicherkrabbe.notifications.notification.Origin;
import de.bennyboer.kicherkrabbe.notifications.notification.Target;
import de.bennyboer.kicherkrabbe.notifications.notification.Title;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SendCmd implements Command {

    Origin origin;

    Target target;

    Set<Channel> channels;

    Title title;

    Message message;

    public static SendCmd of(
            Origin origin,
            Target target,
            Set<Channel> channels,
            Title title,
            Message message
    ) {
        notNull(origin, "Origin must be given");
        notNull(target, "Target must be given");
        notNull(channels, "Channels must be given");
        notNull(title, "Title must be given");
        notNull(message, "Message must be given");

        return new SendCmd(
                origin,
                target,
                channels,
                title,
                message
        );
    }

}
