package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternCategoryName {

    String value;

    public static PatternCategoryName of(String value) {
        notNull(value, "Pattern category name must be given");
        check(!value.isBlank(), "Pattern category name must not be blank");

        return new PatternCategoryName(value);
    }

    @Override
    public String toString() {
        return "PatternCategoryName(%s)".formatted(value);
    }

}
