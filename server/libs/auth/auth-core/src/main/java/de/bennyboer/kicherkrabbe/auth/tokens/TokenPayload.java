package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenPayload {

    Owner owner;

    public static TokenPayload of(Owner owner) {
        notNull(owner, "Owner must be given");

        return new TokenPayload(owner);
    }
    
}
