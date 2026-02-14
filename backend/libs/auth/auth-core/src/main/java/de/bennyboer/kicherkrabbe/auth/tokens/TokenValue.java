package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.security.SecureRandom;
import java.util.Base64;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TokenValue {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    String value;

    public static TokenValue of(String value) {
        notNull(value, "Token value must be given");
        check(!value.isBlank(), "Token value must not be blank");

        return new TokenValue(value);
    }

    public static TokenValue create() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return of(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

}
