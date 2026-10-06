package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryDetails {

    EmbroideryId id;

    Version version;

    boolean published;

    boolean featured;

    EmbroideryName name;

    ImageId image;

    Set<EmbroideryCategoryId> categories;

    Instant createdAt;

    public static EmbroideryDetails of(
            EmbroideryId id,
            Version version,
            boolean published,
            boolean featured,
            EmbroideryName name,
            ImageId image,
            Set<EmbroideryCategoryId> categories,
            Instant createdAt
    ) {
        notNull(id, "Embroidery ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Embroidery name must be given");
        notNull(image, "Image must be given");
        notNull(categories, "Categories must be given");
        notNull(createdAt, "Creation date must be given");

        return new EmbroideryDetails(id, version, published, featured, name, image, categories, createdAt);
    }

}
