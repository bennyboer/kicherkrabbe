package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedEmbroidery {

    EmbroideryId id;

    EmbroideryName name;

    EmbroideryAlias alias;

    ImageId image;

    Set<EmbroideryCategoryId> categories;

    public static PublishedEmbroidery of(
            EmbroideryId id,
            EmbroideryName name,
            EmbroideryAlias alias,
            ImageId image,
            Set<EmbroideryCategoryId> categories
    ) {
        notNull(id, "Embroidery ID must be given");
        notNull(name, "Embroidery name must be given");
        notNull(alias, "Embroidery alias must be given");
        notNull(image, "Image must be given");
        notNull(categories, "Categories must be given");

        return new PublishedEmbroidery(id, name, alias, image, categories);
    }

}
