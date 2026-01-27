package de.bennyboer.kicherkrabbe.products.counter;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CounterId {

    String value;

    public static CounterId of(String value) {
        notNull(value, "Counter ID must be given");
        check(!value.isBlank(), "Counter ID must not be blank");

        return new CounterId(value);
    }

    @Override
    public String toString() {
        return "CounterId(%s)".formatted(value);
    }

}
