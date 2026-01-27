package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternVariantName {

    String value;

    public static PatternVariantName of(String value) {
        notNull(value, "Pattern variant name must be given");
        check(!value.isBlank(), "Pattern variant name must not be blank");

        return new PatternVariantName(value);
    }

    @Override
    public String toString() {
        return "PatternVariantName(%s)".formatted(value);
    }

}
