package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public class SamplePattern {

    @Builder.Default
    private String name = "Sample Pattern " + UUID.randomUUID().toString().substring(0, 8);

    @Builder.Default
    private String number = "S-X-SAM-1";

    @Builder.Default
    private String description = "A sample pattern for testing";

    @Builder.Default
    private SamplePatternAttribution attribution = SamplePatternAttribution.builder().build();

    @Singular
    private Set<String> categories;

    @Singular
    private List<String> images;

    @Singular
    private List<SamplePatternVariant> variants;

    @Singular
    private List<SamplePatternExtra> extras;

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }

    public PatternAttributionDTO getAttributionDTO() {
        return attribution.toDTO();
    }

    public Set<String> getCategories() {
        return categories;
    }

    public List<String> getImages() {
        return images.isEmpty() ? List.of("IMAGE_ID") : images;
    }

    public List<PatternVariantDTO> getVariantDTOs() {
        return variants.isEmpty()
                ? List.of(SamplePatternVariant.builder().build().toDTO())
                : variants.stream().map(SamplePatternVariant::toDTO).toList();
    }

    public List<PatternExtraDTO> getExtraDTOs() {
        return extras.stream().map(SamplePatternExtra::toDTO).toList();
    }

}
