package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupMailPage {

    long total;

    List<LookupMail> mails;

    public static LookupMailPage of(long total, List<LookupMail> mails) {
        check(total >= 0, "total must be greater or equal to 0");
        notNull(mails, "Mails must be given");

        return new LookupMailPage(total, mails);
    }

}
