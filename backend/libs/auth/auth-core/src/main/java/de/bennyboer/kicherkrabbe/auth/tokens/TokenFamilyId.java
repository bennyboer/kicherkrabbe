package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TokenFamilyId {

    String value;

    public static TokenFamilyId of(String value) {
        notNull(value, "Token family ID must be given");
        check(!value.isBlank(), "Token family ID must not be blank");

        return new TokenFamilyId(value);
    }

    public static TokenFamilyId create() {
        return of(UUID.randomUUID().toString());
    }

}
