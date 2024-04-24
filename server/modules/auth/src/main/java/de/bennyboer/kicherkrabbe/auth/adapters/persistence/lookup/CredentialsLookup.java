package de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.Name;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CredentialsLookup {

    CredentialsId id;

    Name name;

    public static CredentialsLookup of(CredentialsId id, Name name) {
        notNull(id, "Credentials ID must be given");
        notNull(name, "Name must be given");

        return new CredentialsLookup(id, name);
    }

}
