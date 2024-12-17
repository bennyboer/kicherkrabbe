package de.bennyboer.kicherkrabbe.inquiries.send;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.inquiries.Message;
import de.bennyboer.kicherkrabbe.inquiries.Sender;
import de.bennyboer.kicherkrabbe.inquiries.Subject;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SendCmd implements Command {

    Sender sender;

    Subject subject;

    Message message;

    public static SendCmd of(Sender sender, Subject subject, Message message) {
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(message, "Message must be given");

        return new SendCmd(sender, subject, message);
    }

}
