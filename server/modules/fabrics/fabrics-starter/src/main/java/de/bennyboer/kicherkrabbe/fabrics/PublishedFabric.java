package de.bennyboer.kicherkrabbe.fabrics;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedFabric {

    FabricId id;

    FabricName name;

    ImageId image;

    Set<ColorId> colors;

    Set<TopicId> topics;

    Set<FabricTypeAvailability> availability;

    public static PublishedFabric of(
            FabricId id,
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability
    ) {
        notNull(id, "Fabric ID must be given");
        notNull(name, "Fabric name must be given");
        notNull(image, "Fabric image must be given");
        notNull(colors, "Fabric colors must be given");
        notNull(topics, "Fabric topics must be given");
        notNull(availability, "Fabric availability must be given");

        return new PublishedFabric(id, name, image, colors, topics, availability);
    }

}
