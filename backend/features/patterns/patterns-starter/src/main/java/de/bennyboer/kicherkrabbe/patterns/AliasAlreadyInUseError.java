package de.bennyboer.kicherkrabbe.patterns;

import lombok.Getter;

@Getter
public class AliasAlreadyInUseError extends Exception {

    private final PatternId conflictingPatternId;

    private final PatternAlias alias;

    public AliasAlreadyInUseError(PatternId conflictingPatternId, PatternAlias alias) {
        super("The pattern alias '%s' is already in use for pattern with ID '%s'".formatted(
                alias.getValue(),
                conflictingPatternId.getValue()
        ));

        this.conflictingPatternId = conflictingPatternId;
        this.alias = alias;
    }

}
