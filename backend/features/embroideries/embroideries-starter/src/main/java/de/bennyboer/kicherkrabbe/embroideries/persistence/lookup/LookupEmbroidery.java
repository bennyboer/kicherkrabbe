package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupEmbroidery implements VersionedReadModel<EmbroideryId> {

    EmbroideryId id;

    Version version;

    boolean published;

    boolean featured;

    EmbroideryName name;

    EmbroideryAlias alias;

    ImageId image;

    Set<EmbroideryCategoryId> categories;

    Instant createdAt;

    public static LookupEmbroidery of(
            EmbroideryId id,
            Version version,
            boolean published,
            boolean featured,
            EmbroideryName name,
            EmbroideryAlias alias,
            ImageId image,
            Set<EmbroideryCategoryId> categories,
            Instant createdAt
    ) {
        notNull(id, "Embroidery ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(alias, "Alias must be given");
        notNull(image, "Image must be given");
        notNull(categories, "Categories must be given");
        notNull(createdAt, "Creation date must be given");

        return new LookupEmbroidery(
                id,
                version,
                published,
                featured,
                name,
                alias,
                image,
                categories,
                createdAt
        );
    }

}
