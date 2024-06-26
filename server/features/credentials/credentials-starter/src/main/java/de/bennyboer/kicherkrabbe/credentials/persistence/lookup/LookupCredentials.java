package de.bennyboer.kicherkrabbe.credentials.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.credentials.UserId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupCredentials {

    CredentialsId id;

    Name name;

    UserId userId;

    public static LookupCredentials of(CredentialsId id, Name name, UserId userId) {
        notNull(id, "Credentials ID must be given");
        notNull(name, "Name must be given");
        notNull(userId, "User ID must be given");

        return new LookupCredentials(id, name, userId);
    }

}
