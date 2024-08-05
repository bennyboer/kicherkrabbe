package de.bennyboer.kicherkrabbe.patterns;

import lombok.Getter;

@Getter
public class PatternNotFoundError extends Exception {

    private final PatternId patternId;

    public PatternNotFoundError(PatternId patternId) {
        super("Pattern with ID '%s' not found".formatted(patternId.getValue()));
        this.patternId = patternId;
    }

}
