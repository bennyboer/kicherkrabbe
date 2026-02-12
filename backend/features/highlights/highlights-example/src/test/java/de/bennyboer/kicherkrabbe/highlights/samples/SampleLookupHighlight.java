package de.bennyboer.kicherkrabbe.highlights.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.highlights.HighlightId;
import de.bennyboer.kicherkrabbe.highlights.ImageId;
import de.bennyboer.kicherkrabbe.highlights.Links;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlight;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleLookupHighlight {

    @Builder.Default
    private HighlightId id = HighlightId.create();

    @Builder.Default
    private Version version = Version.zero();

    @Builder.Default
    private String imageId = "IMAGE_ID";

    @Singular
    private Set<SampleLink> links;

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private long sortOrder = 100L;

    @Builder.Default
    private Instant createdAt = Instant.parse("2024-12-10T12:30:00.000Z");

    public LookupHighlight toValue() {
        return LookupHighlight.of(
                id,
                version,
                ImageId.of(imageId),
                getLinks(),
                published,
                sortOrder,
                createdAt
        );
    }

    private Links getLinks() {
        if (links.isEmpty()) {
            return Links.of(Set.of());
        }
        return Links.of(links.stream()
                .map(SampleLink::toValue)
                .collect(Collectors.toSet()));
    }

}
