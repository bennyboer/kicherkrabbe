package de.bennyboer.kicherkrabbe.mailbox.mail.receive;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.mailbox.mail.Content;
import de.bennyboer.kicherkrabbe.mailbox.mail.Origin;
import de.bennyboer.kicherkrabbe.mailbox.mail.Sender;
import de.bennyboer.kicherkrabbe.mailbox.mail.Subject;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ReceiveCmd implements Command {

    Origin origin;

    Sender sender;

    Subject subject;

    Content content;

    public static ReceiveCmd of(
            Origin origin,
            Sender sender,
            Subject subject,
            Content content
    ) {
        notNull(origin, "Origin must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(content, "Content must be given");

        return new ReceiveCmd(
                origin,
                sender,
                subject,
                content
        );
    }

}
