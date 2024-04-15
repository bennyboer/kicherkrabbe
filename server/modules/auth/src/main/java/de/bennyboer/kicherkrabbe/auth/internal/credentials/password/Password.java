package de.bennyboer.kicherkrabbe.auth.internal.credentials.password;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Password {

    String value;

    public static Password of(String value) {
        notNull(value, "Password must be given");
        check(!value.isBlank(), "Password must not be blank");
        check(value.length() >= 8, "Password must be at least 8 characters long");

        return new Password(value);
    }

    @Override
    public String toString() {
        return "Password(%s)".formatted("********");
    }

}
