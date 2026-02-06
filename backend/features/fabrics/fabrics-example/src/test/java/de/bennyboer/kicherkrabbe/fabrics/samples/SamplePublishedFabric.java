package de.bennyboer.kicherkrabbe.fabrics.samples;

import de.bennyboer.kicherkrabbe.fabrics.*;
import lombok.Builder;
import lombok.Singular;

import java.util.Set;

@Builder
public class SamplePublishedFabric {

    @Builder.Default
    private FabricId id = FabricId.of("FABRIC_ID");

    @Builder.Default
    private FabricName name = FabricName.of("Sample Fabric");

    @Builder.Default
    private FabricAlias alias = FabricAlias.of("sample-fabric");

    @Builder.Default
    private ImageId image = ImageId.of("IMAGE_ID");

    @Singular
    private Set<ColorId> colors;

    @Singular
    private Set<TopicId> topics;

    @Singular("availability")
    private Set<FabricTypeAvailability> availabilities;

    public PublishedFabric toModel() {
        return PublishedFabric.of(
                id,
                name,
                alias,
                image,
                colors,
                topics,
                availabilities
        );
    }

}
