package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternDesigner {

    String value;

    public static PatternDesigner of(String value) {
        notNull(value, "Pattern designer must be given");
        check(!value.isBlank(), "Pattern designer must not be blank");

        return new PatternDesigner(value);
    }

    @Override
    public String toString() {
        return "PatternDesigner(%s)".formatted(value);
    }

}
