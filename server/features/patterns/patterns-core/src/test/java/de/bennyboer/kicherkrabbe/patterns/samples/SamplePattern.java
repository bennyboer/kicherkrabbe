package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.*;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SamplePattern {

    @Builder.Default
    private String name = "Sample Pattern";

    @Builder.Default
    private String number = "S-X-SAM-1";

    @Builder.Default
    private String description = null;

    @Builder.Default
    private SamplePatternAttribution attribution = SamplePatternAttribution.builder().build();

    @Singular
    private Set<String> categoryIds;

    @Singular
    private List<String> imageIds;

    @Singular
    private List<SamplePatternVariant> variants;

    @Singular
    private List<SamplePatternExtra> extras;

    public PatternName getName() {
        return PatternName.of(name);
    }

    public PatternNumber getNumber() {
        return PatternNumber.of(number);
    }

    public PatternDescription getDescription() {
        return description != null ? PatternDescription.of(description) : null;
    }

    public PatternAttribution getAttribution() {
        return attribution.toValue();
    }

    public Set<PatternCategoryId> getCategoryIds() {
        return categoryIds.stream()
                .map(PatternCategoryId::of)
                .collect(Collectors.toSet());
    }

    public List<ImageId> getImageIds() {
        if (imageIds.isEmpty()) {
            return List.of(ImageId.of("IMAGE_ID"));
        }
        return imageIds.stream()
                .map(ImageId::of)
                .toList();
    }

    public List<PatternVariant> getVariants() {
        if (variants.isEmpty()) {
            return List.of(SamplePatternVariant.builder().build().toValue());
        }
        return variants.stream()
                .map(SamplePatternVariant::toValue)
                .toList();
    }

    public List<PatternExtra> getExtras() {
        return extras.stream()
                .map(SamplePatternExtra::toValue)
                .toList();
    }

}
