package de.bennyboer.kicherkrabbe.users.persistence.lookup;

import de.bennyboer.kicherkrabbe.users.FullName;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.users.UserId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupUser {

    UserId userId;

    FullName name;

    Mail mail;

    public static LookupUser of(UserId userId, FullName name, Mail mail) {
        notNull(userId, "User ID must be given");
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");

        return new LookupUser(userId, name, mail);
    }

}
