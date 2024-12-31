package de.bennyboer.kicherkrabbe.mailing.external;

import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import de.bennyboer.kicherkrabbe.mailing.settings.Subject;
import de.bennyboer.kicherkrabbe.mailing.settings.Text;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SentMail {

    EMail from;

    EMail to;

    Subject subject;

    Text text;

    public static SentMail of(
            EMail from,
            EMail to,
            Subject subject,
            Text text
    ) {
        notNull(from, "From email must be given");
        notNull(to, "To email must be given");
        notNull(subject, "Subject must be given");
        notNull(text, "Text must be given");

        return new SentMail(from, to, subject, text);
    }

}
