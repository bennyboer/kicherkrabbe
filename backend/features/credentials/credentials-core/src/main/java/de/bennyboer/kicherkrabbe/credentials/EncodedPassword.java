package de.bennyboer.kicherkrabbe.credentials;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EncodedPassword {

    String value;

    public static EncodedPassword of(String value) {
        notNull(value, "EncodedPassword must be given");
        check(!value.isBlank(), "EncodedPassword must not be blank");

        return new EncodedPassword(value);
    }

    @Override
    public String toString() {
        return "EncodedPassword(%s)".formatted(value);
    }

}
