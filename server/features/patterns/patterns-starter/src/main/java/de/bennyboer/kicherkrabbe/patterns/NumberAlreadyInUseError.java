package de.bennyboer.kicherkrabbe.patterns;

import lombok.Getter;

@Getter
public class NumberAlreadyInUseError extends Exception {

    private final PatternId conflictingPatternId;

    private final PatternNumber number;

    public NumberAlreadyInUseError(PatternId conflictingPatternId, PatternNumber number) {
        super("The pattern number '%s' is already in use for pattern with ID '%s'".formatted(
                number.getValue(),
                conflictingPatternId.getValue()
        ));

        this.conflictingPatternId = conflictingPatternId;
        this.number = number;
    }

}
