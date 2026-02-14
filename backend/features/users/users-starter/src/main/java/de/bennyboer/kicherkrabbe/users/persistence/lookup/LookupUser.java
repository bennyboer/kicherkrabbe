package de.bennyboer.kicherkrabbe.users.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.users.FullName;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.commons.UserId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupUser implements VersionedReadModel<UserId> {

    UserId id;

    Version version;

    FullName name;

    Mail mail;

    public static LookupUser of(UserId id, Version version, FullName name, Mail mail) {
        notNull(id, "User ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");

        return new LookupUser(id, version, name, mail);
    }

}
