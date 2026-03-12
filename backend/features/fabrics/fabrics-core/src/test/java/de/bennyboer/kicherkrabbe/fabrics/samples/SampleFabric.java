package de.bennyboer.kicherkrabbe.fabrics.samples;

import de.bennyboer.kicherkrabbe.fabrics.*;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleFabric {

    @Builder.Default
    private String name = "Sample Fabric";

    @Builder.Default
    private FabricKind kind = FabricKind.PATTERNED;

    @Builder.Default
    private String imageId = "IMAGE_ID";

    @Nullable
    @Builder.Default
    private List<String> exampleImageIds = null;

    @Singular
    private Set<String> colorIds;

    @Singular
    private Set<String> topicIds;

    @Singular
    private Set<SampleFabricTypeAvailability> availabilities;

    public FabricName getName() {
        return FabricName.of(name);
    }

    public FabricKind getKind() {
        return kind;
    }

    public ImageId getImageId() {
        return ImageId.of(imageId);
    }

    @Nullable
    public List<ImageId> getExampleImageIds() {
        if (exampleImageIds == null) {
            return null;
        }
        return exampleImageIds.stream()
                .map(ImageId::of)
                .toList();
    }

    public Set<ColorId> getColorIds() {
        if (colorIds.isEmpty()) {
            return Set.of(ColorId.of("COLOR_ID"));
        }
        return colorIds.stream()
                .map(ColorId::of)
                .collect(Collectors.toSet());
    }

    public Set<TopicId> getTopicIds() {
        if (topicIds.isEmpty()) {
            return Set.of(TopicId.of("TOPIC_ID"));
        }
        return topicIds.stream()
                .map(TopicId::of)
                .collect(Collectors.toSet());
    }

    public Set<FabricTypeAvailability> getAvailabilities() {
        if (availabilities.isEmpty()) {
            return Set.of(SampleFabricTypeAvailability.builder().build().toValue());
        }
        return availabilities.stream()
                .map(SampleFabricTypeAvailability::toValue)
                .collect(Collectors.toSet());
    }

}
