package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternNumber {

    private static final String PREFIX = "S-";

    String value;

    public static PatternNumber of(String value) {
        notNull(value, "Pattern name must be given");
        check(!value.isBlank(), "Pattern name must not be blank");
        check(value.startsWith(PREFIX), "Pattern number must start with %s".formatted(PREFIX));

        return new PatternNumber(value);
    }

    @Override
    public String toString() {
        return "PatternName(%s)".formatted(value);
    }

}
