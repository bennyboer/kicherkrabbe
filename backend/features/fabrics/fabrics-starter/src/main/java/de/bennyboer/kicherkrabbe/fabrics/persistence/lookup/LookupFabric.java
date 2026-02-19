package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.fabrics.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupFabric implements VersionedReadModel<FabricId> {

    FabricId id;

    Version version;

    FabricName name;

    FabricAlias alias;

    ImageId image;

    List<ImageId> exampleImages;

    Set<ColorId> colors;

    Set<TopicId> topics;

    Set<FabricTypeAvailability> availability;

    boolean published;

    boolean featured;

    Instant createdAt;

    public static LookupFabric of(
            FabricId id,
            Version version,
            FabricName name,
            FabricAlias alias,
            ImageId image,
            List<ImageId> exampleImages,
            Set<ColorId> colors,
            Set<TopicId> topics,
            Set<FabricTypeAvailability> availability,
            boolean published,
            boolean featured,
            Instant createdAt
    ) {
        notNull(id, "Fabric ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(alias, "Alias must be given");
        notNull(image, "Image ID must be given");
        notNull(exampleImages, "Example image IDs must be given");
        notNull(colors, "Color IDs must be given");
        notNull(topics, "Topic IDs must be given");
        notNull(availability, "Availability must be given");
        notNull(createdAt, "Created at must be given");

        return new LookupFabric(id, version, name, alias, image, exampleImages, colors, topics, availability, published, featured, createdAt);
    }

}
