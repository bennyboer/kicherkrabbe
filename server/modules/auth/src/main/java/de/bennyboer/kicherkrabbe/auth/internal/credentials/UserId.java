package de.bennyboer.kicherkrabbe.auth.internal.credentials;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UserId {

    String value;

    public static UserId of(String value) {
        notNull(value, "UserId must be given");
        check(!value.isBlank(), "UserId must not be blank");

        return new UserId(value);
    }

    @Override
    public String toString() {
        return "UserId(%s)".formatted(value);
    }

}

