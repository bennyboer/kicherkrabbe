package de.bennyboer.kicherkrabbe.mailing.mail;

import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Receiver {

    EMail mail;

    public static Receiver of(EMail mail) {
        notNull(mail, "Mail must be given");

        return new Receiver(mail);
    }

    public Receiver anonymized() {
        return withMail(mail.anonymized());
    }

}
