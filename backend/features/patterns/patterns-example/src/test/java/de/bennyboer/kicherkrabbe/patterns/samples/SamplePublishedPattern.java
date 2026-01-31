package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;

@Builder
public class SamplePublishedPattern {

    @Builder.Default
    private PatternId id = PatternId.of("PATTERN_ID");

    @Builder.Default
    private PatternName name = PatternName.of("Sample Pattern");

    @Builder.Default
    private PatternNumber number = PatternNumber.of("S-X-SAM-1");

    @Nullable
    @Builder.Default
    private PatternDescription description = null;

    @Builder.Default
    private PatternAlias alias = PatternAlias.of("sample-pattern");

    @Builder.Default
    private PatternAttribution attribution = PatternAttribution.of(
            OriginalPatternName.of("Original Pattern"),
            PatternDesigner.of("Sample Designer")
    );

    @Singular
    private Set<PatternCategoryId> categories;

    @Singular
    private List<ImageId> images;

    @Singular
    private List<PatternVariant> variants;

    @Singular
    private List<PatternExtra> extras;

    public PublishedPattern toModel() {
        return PublishedPattern.of(
                id,
                name,
                number,
                description,
                alias,
                attribution,
                categories,
                images.isEmpty() ? List.of(ImageId.of("IMAGE_ID")) : images,
                variants.isEmpty()
                        ? List.of(PatternVariant.of(
                        PatternVariantName.of("Default"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                        : variants,
                extras
        );
    }

}
