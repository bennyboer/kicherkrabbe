package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import de.bennyboer.kicherkrabbe.credentials.internal.UserId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CredentialsLookup {

    CredentialsId id;

    Name name;

    UserId userId;

    public static CredentialsLookup of(CredentialsId id, Name name, UserId userId) {
        notNull(id, "Credentials ID must be given");
        notNull(name, "Name must be given");
        notNull(userId, "User ID must be given");

        return new CredentialsLookup(id, name, userId);
    }

}
