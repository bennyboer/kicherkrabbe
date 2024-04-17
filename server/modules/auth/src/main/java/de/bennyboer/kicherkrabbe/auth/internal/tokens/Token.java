package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Token {

    String value;

    public static Token of(String value) {
        notNull(value, "Token value must be given");
        check(!value.isBlank(), "Token value must not be blank");

        return new Token(value);
    }

}
