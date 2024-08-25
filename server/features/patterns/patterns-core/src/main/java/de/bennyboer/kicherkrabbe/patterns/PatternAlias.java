package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternName {

    String value;

    public static PatternName of(String value) {
        notNull(value, "Pattern name must be given");
        check(!value.isBlank(), "Pattern name must not be blank");

        return new PatternName(value);
    }

    @Override
    public String toString() {
        return "PatternName(%s)".formatted(value);
    }

}
