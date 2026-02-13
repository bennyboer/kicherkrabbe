package de.bennyboer.kicherkrabbe.highlights.samples;

import de.bennyboer.kicherkrabbe.highlights.ImageId;
import de.bennyboer.kicherkrabbe.highlights.Links;
import lombok.Builder;
import lombok.Singular;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleHighlight {

    @Builder.Default
    private String imageId = "IMAGE_ID";

    @Builder.Default
    private long sortOrder = 0L;

    @Singular
    private Set<SampleLink> links;

    public ImageId getImageId() {
        return ImageId.of(imageId);
    }

    public long getSortOrder() {
        return sortOrder;
    }

    public Links getLinks() {
        if (links.isEmpty()) {
            return Links.of(Set.of());
        }
        return Links.of(links.stream()
                .map(SampleLink::toValue)
                .collect(Collectors.toSet()));
    }

}
