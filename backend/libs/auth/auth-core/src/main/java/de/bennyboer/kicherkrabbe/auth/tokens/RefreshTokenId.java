package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RefreshTokenId {

    String value;

    public static RefreshTokenId of(String value) {
        notNull(value, "Refresh token ID must be given");
        check(!value.isBlank(), "Refresh token ID must not be blank");

        return new RefreshTokenId(value);
    }

    public static RefreshTokenId create() {
        return of(UUID.randomUUID().toString());
    }

}
