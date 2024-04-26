package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Owner {

    OwnerId id;

    OwnerType type;

    public static Owner system() {
        return new Owner(OwnerId.of("SYSTEM"), OwnerType.SYSTEM);
    }

    public static Owner of(OwnerId id) {
        notNull(id, "Owner id must be given");

        return new Owner(id, OwnerType.USER);
    }

}
