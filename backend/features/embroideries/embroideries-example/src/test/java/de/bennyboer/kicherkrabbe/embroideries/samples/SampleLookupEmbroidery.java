package de.bennyboer.kicherkrabbe.embroideries.samples;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.Set;

@Builder
public class SampleLookupEmbroidery {

    @Builder.Default
    private EmbroideryId id = EmbroideryId.create();

    @Builder.Default
    private Version version = Version.zero();

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private boolean featured = false;

    @Builder.Default
    private EmbroideryName name = EmbroideryName.of("Sample Embroidery");

    @Nullable
    @Builder.Default
    private EmbroideryAlias alias = null;

    @Builder.Default
    private ImageId image = ImageId.of("IMAGE_ID");

    @Singular
    private Set<EmbroideryCategoryId> categories;

    @Builder.Default
    private Instant createdAt = Instant.parse("2024-03-12T12:30:00.00Z");

    public LookupEmbroidery toModel() {
        return LookupEmbroidery.of(
                id,
                version,
                published,
                featured,
                name,
                alias != null ? alias : EmbroideryAlias.of("sample-embroidery-" + id.getValue()),
                image,
                categories,
                createdAt
        );
    }

}
