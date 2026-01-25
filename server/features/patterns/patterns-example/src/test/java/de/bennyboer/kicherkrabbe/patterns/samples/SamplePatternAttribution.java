package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import lombok.Builder;

@Builder
public class SamplePatternAttribution {

    @Builder.Default
    private String originalPatternName = null;

    @Builder.Default
    private String designer = null;

    public PatternAttributionDTO toDTO() {
        var dto = new PatternAttributionDTO();
        dto.originalPatternName = originalPatternName;
        dto.designer = designer;
        return dto;
    }

}
