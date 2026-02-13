package de.bennyboer.kicherkrabbe.highlights.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.highlights.*;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleHighlightDetails {

    @Builder.Default
    private String id = "HIGHLIGHT_ID";

    @Builder.Default
    private long version = 0L;

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

    public HighlightDetails toValue() {
        return HighlightDetails.of(
                HighlightId.of(id),
                Version.of(version),
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
