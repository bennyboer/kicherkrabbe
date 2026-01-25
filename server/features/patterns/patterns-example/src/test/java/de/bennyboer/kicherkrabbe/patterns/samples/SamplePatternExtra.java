package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import lombok.Builder;

@Builder
public class SamplePatternExtra {

    @Builder.Default
    private String name = "Extra";

    @Builder.Default
    private SampleMoney price = SampleMoney.builder().amount(200).build();

    public PatternExtraDTO toDTO() {
        var dto = new PatternExtraDTO();
        dto.name = name;
        dto.price = price.toDTO();
        return dto;
    }

}
