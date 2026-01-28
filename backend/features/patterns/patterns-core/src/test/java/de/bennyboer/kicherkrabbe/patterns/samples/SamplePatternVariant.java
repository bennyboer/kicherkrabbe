package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.PatternVariant;
import de.bennyboer.kicherkrabbe.patterns.PatternVariantName;
import de.bennyboer.kicherkrabbe.patterns.PricedSizeRange;
import lombok.Builder;
import lombok.Singular;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SamplePatternVariant {

    @Builder.Default
    private String name = "Sample Variant";

    @Singular
    private Set<SamplePricedSizeRange> pricedSizeRanges;

    public PatternVariant toValue() {
        return PatternVariant.of(
                PatternVariantName.of(name),
                getPricedSizeRanges()
        );
    }

    private Set<PricedSizeRange> getPricedSizeRanges() {
        if (pricedSizeRanges.isEmpty()) {
            return Set.of(SamplePricedSizeRange.builder().build().toValue());
        }
        return pricedSizeRanges.stream()
                .map(SamplePricedSizeRange::toValue)
                .collect(Collectors.toSet());
    }

}
