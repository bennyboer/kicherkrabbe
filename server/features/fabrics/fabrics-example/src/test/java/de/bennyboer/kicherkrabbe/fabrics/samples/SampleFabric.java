package de.bennyboer.kicherkrabbe.fabrics.samples;

import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;

@Builder
public class SampleFabric {

    @Builder.Default
    private String name = "Sample Fabric";

    @Builder.Default
    private String imageId = "SAMPLE_IMAGE_ID";

    @Singular
    private Set<String> colorIds;

    @Singular
    private Set<String> topicIds;

    @Singular
    private List<SampleFabricTypeAvailability> availabilities;

    public String getName() {
        return name;
    }

    public String getImageId() {
        return imageId;
    }

    public Set<String> getColorIds() {
        return colorIds;
    }

    public Set<String> getTopicIds() {
        return topicIds;
    }

    public Set<FabricTypeAvailabilityDTO> getAvailabilityDTOs() {
        if (availabilities.isEmpty()) {
            return Set.of(
                    SampleFabricTypeAvailability.builder()
                            .typeId("JERSEY_ID")
                            .inStock(true)
                            .build()
                            .toDTO()
            );
        }
        return availabilities.stream()
                .map(SampleFabricTypeAvailability::toDTO)
                .collect(java.util.stream.Collectors.toSet());
    }

}
