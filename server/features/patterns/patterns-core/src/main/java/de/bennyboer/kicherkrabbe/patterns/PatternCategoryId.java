package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternCategoryId {

    String value;

    public static PatternCategoryId of(String value) {
        notNull(value, "Pattern category ID must be given");
        check(!value.isBlank(), "Pattern category ID must not be blank");

        return new PatternCategoryId(value);
    }

    @Override
    public String toString() {
        return "PatternCategoryId(%s)".formatted(value);
    }

}
