package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternDescription {

    String value;

    public static PatternDescription of(String value) {
        notNull(value, "Pattern description must be given");
        check(!value.isBlank(), "Pattern description must not be blank");

        return new PatternDescription(value);
    }

    @Override
    public String toString() {
        return "PatternDescription(%s)".formatted(value);
    }

}
