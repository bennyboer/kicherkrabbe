package de.bennyboer.kicherkrabbe.auth.internal.credentials;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Name {

    String value;

    public static Name of(String value) {
        notNull(value, "Name must be given");
        check(!value.isBlank(), "Name must not be blank");

        return new Name(value);
    }

    @Override
    public String toString() {
        return "Name(%s)".formatted(value);
    }

}
