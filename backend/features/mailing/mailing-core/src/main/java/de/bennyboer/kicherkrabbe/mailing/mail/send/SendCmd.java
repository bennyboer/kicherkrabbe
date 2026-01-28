package de.bennyboer.kicherkrabbe.mailing.mail.send;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.mailing.MailingService;
import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SendCmd implements Command {

    Sender sender;

    Set<Receiver> receivers;

    Subject subject;

    Text text;

    MailingService mailingService;

    public static SendCmd of(
            Sender sender,
            Set<Receiver> receivers,
            Subject subject,
            Text text,
            MailingService mailingService
    ) {
        notNull(sender, "Sender must be given");
        notNull(receivers, "Receivers must be given");
        check(!receivers.isEmpty(), "At least one receiver must be given");
        notNull(subject, "Subject must be given");
        notNull(text, "Text must be given");
        notNull(mailingService, "Mailing service must be given");

        return new SendCmd(
                sender,
                receivers,
                subject,
                text,
                mailingService
        );
    }

}
