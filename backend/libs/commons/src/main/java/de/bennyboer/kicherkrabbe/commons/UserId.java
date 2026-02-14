package de.bennyboer.kicherkrabbe.commons;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UserId {

    String value;

    public static UserId of(String value) {
        notNull(value, "User ID must be given");
        check(!value.isBlank(), "User ID must not be blank");

        return new UserId(value);
    }

    public static UserId create() {
        return new UserId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "UserId(%s)".formatted(value);
    }

}
