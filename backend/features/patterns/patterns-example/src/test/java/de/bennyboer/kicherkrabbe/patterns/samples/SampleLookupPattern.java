package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Builder
public class SampleLookupPattern {

    @Builder.Default
    private PatternId id = PatternId.create();

    @Builder.Default
    private Version version = Version.zero();

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private boolean featured = false;

    @Builder.Default
    private PatternName name = PatternName.of("Sample Pattern");

    @Nullable
    @Builder.Default
    private PatternNumber number = null;

    @Nullable
    @Builder.Default
    private PatternDescription description = null;

    @Nullable
    @Builder.Default
    private PatternAlias alias = null;

    @Builder.Default
    private PatternAttribution attribution = PatternAttribution.of(null, null);

    @Singular
    private Set<PatternCategoryId> categories;

    @Singular
    private List<ImageId> images;

    @Singular
    private List<PatternVariant> variants;

    @Singular
    private List<PatternExtra> extras;

    @Builder.Default
    private Instant createdAt = Instant.parse("2024-03-12T12:30:00.00Z");

    public LookupPattern toModel() {
        return LookupPattern.of(
                id,
                version,
                published,
                featured,
                name,
                number != null ? number : PatternNumber.of("S-X-SAM-" + id.getValue().substring(0, 8)),
                description,
                alias != null ? alias : PatternAlias.of("sample-pattern-" + id.getValue()),
                attribution,
                categories,
                images.isEmpty() ? List.of(ImageId.of("IMAGE_ID")) : images,
                variants.isEmpty()
                        ? List.of(PatternVariant.of(
                                PatternVariantName.of("Default"),
                                Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                        ))
                        : variants,
                extras,
                createdAt
        );
    }

}
