package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.fabrics.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupFabric {

    FabricId id;

    Version version;

    FabricName name;

    ImageId image;

    Set<ColorId> colors;

    Set<TopicId> topics;

    Set<FabricTypeAvailability> availability;

    boolean published;

    Instant createdAt;

    public static LookupFabric of(
            FabricId id,
            Version version,
            FabricName name,
            ImageId image,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability,
            boolean published,
            Instant createdAt
    ) {
        notNull(id, "Fabric ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(image, "Image ID must be given");
        notNull(colors, "Color IDs must be given");
        notNull(topics, "Topic IDs must be given");
        notNull(availability, "Availability must be given");
        notNull(createdAt, "Created at must be given");

        return new LookupFabric(id, version, name, image, colors, topics, availability, published, createdAt);
    }

}
