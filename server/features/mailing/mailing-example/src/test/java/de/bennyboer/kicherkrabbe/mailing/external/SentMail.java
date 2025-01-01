package de.bennyboer.kicherkrabbe.mailing.external;

import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SentMail {

    Sender sender;

    Set<Receiver> receivers;

    Subject subject;

    Text text;

    public static SentMail of(
            Sender sender,
            Set<Receiver> receivers,
            Subject subject,
            Text text
    ) {
        notNull(sender, "Sender must be given");
        notNull(receivers, "Receivers must be given");
        notNull(subject, "Subject must be given");
        notNull(text, "Text must be given");

        return new SentMail(sender, receivers, subject, text);
    }

}
