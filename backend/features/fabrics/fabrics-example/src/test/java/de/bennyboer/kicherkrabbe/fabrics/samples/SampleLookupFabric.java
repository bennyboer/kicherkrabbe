package de.bennyboer.kicherkrabbe.fabrics.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabric;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.Set;

@Builder
public class SampleLookupFabric {

    @Builder.Default
    private FabricId id = FabricId.create();

    @Builder.Default
    private Version version = Version.zero();

    @Builder.Default
    private FabricName name = FabricName.of("Sample Fabric");

    @Builder.Default
    private FabricAlias alias = FabricAlias.of("sample-fabric");

    @Builder.Default
    private ImageId image = ImageId.of("SAMPLE_IMAGE_ID");

    @Singular
    private Set<ColorId> colors;

    @Singular
    private Set<TopicId> topics;

    @Singular("availability")
    private Set<FabricTypeAvailability> availabilities;

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private boolean featured = false;

    @Builder.Default
    private Instant createdAt = Instant.parse("2024-03-12T12:30:00.00Z");

    public LookupFabric toModel() {
        return LookupFabric.of(
                id,
                version,
                name,
                alias,
                image,
                colors.isEmpty() ? Set.of(ColorId.of("COLOR_ID")) : colors,
                topics.isEmpty() ? Set.of(TopicId.of("TOPIC_ID")) : topics,
                availabilities.isEmpty()
                        ? Set.of(FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true))
                        : availabilities,
                published,
                featured,
                createdAt
        );
    }

}
