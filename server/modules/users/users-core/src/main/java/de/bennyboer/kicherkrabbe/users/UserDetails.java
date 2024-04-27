package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.users.internal.FullName;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
import de.bennyboer.kicherkrabbe.users.internal.UserId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UserDetails {

    UserId userId;

    FullName name;

    Mail mail;

    public static UserDetails of(UserId userId, FullName name, Mail mail) {
        notNull(userId, "User ID must be given");
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");

        return new UserDetails(userId, name, mail);
    }

}
