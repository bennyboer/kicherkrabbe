package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.OriginalPatternName;
import de.bennyboer.kicherkrabbe.patterns.PatternAttribution;
import de.bennyboer.kicherkrabbe.patterns.PatternDesigner;
import lombok.Builder;

@Builder
public class SamplePatternAttribution {

    @Builder.Default
    private String originalPatternName = null;

    @Builder.Default
    private String designer = null;

    public PatternAttribution toValue() {
        if (originalPatternName == null && designer == null) {
            return PatternAttribution.empty();
        }
        return PatternAttribution.of(
                originalPatternName != null ? OriginalPatternName.of(originalPatternName) : null,
                designer != null ? PatternDesigner.of(designer) : null
        );
    }

}
