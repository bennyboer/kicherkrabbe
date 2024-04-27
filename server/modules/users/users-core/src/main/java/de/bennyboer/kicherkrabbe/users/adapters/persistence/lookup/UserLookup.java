package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.users.internal.FullName;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
import de.bennyboer.kicherkrabbe.users.internal.UserId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UserLookup {

    UserId userId;

    FullName name;

    Mail mail;

    public static UserLookup of(UserId userId, FullName name, Mail mail) {
        notNull(userId, "User ID must be given");
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");

        return new UserLookup(userId, name, mail);
    }

}
