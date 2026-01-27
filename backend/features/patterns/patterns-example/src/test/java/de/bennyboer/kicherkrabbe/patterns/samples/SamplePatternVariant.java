package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import lombok.Builder;
import lombok.Singular;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SamplePatternVariant {

    @Builder.Default
    private String name = "Normal";

    @Singular
    private Set<SamplePricedSizeRange> pricedSizeRanges;

    public PatternVariantDTO toDTO() {
        var dto = new PatternVariantDTO();
        dto.name = name;
        dto.pricedSizeRanges = pricedSizeRanges.isEmpty()
                ? Set.of(SamplePricedSizeRange.builder().build().toDTO())
                : pricedSizeRanges.stream()
                        .map(SamplePricedSizeRange::toDTO)
                        .collect(Collectors.toSet());
        return dto;
    }

}
