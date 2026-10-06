package de.bennyboer.kicherkrabbe.embroideries.samples;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import lombok.Builder;
import lombok.Singular;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleEmbroidery {

    @Builder.Default
    private String name = "Sample Embroidery";

    @Builder.Default
    private String imageId = "IMAGE_ID";

    @Singular
    private Set<String> categoryIds;

    public EmbroideryName getName() {
        return EmbroideryName.of(name);
    }

    public ImageId getImageId() {
        return ImageId.of(imageId);
    }

    public Set<EmbroideryCategoryId> getCategoryIds() {
        if (categoryIds.isEmpty()) {
            return Set.of(EmbroideryCategoryId.of("CATEGORY_ID"));
        }

        return categoryIds.stream()
                .map(EmbroideryCategoryId::of)
                .collect(Collectors.toSet());
    }

}
