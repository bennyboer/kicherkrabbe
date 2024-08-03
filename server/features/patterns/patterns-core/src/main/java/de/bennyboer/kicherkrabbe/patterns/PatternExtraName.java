package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternExtraName {

    String value;

    public static PatternExtraName of(String value) {
        notNull(value, "Pattern extra name must be given");
        check(!value.isBlank(), "Pattern extra name must not be blank");

        return new PatternExtraName(value);
    }

    @Override
    public String toString() {
        return "PatternExtraName(%s)".formatted(value);
    }

}
