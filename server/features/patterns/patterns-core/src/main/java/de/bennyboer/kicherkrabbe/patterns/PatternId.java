package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternId {

    String value;

    public static PatternId of(String value) {
        notNull(value, "Pattern ID must be given");
        check(!value.isBlank(), "Pattern ID must not be blank");

        return new PatternId(value);
    }

    public static PatternId create() {
        return new PatternId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "PatternId(%s)".formatted(value);
    }

}
