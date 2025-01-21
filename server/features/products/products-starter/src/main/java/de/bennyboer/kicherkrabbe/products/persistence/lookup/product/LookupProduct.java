package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.products.product.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupProduct {

    ProductId id;

    Version version;

    ProductNumber number;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Notes notes;

    Instant producedAt;

    Instant createdAt;

    public static LookupProduct of(
            ProductId id,
            Version version,
            ProductNumber number,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Notes notes,
            Instant producedAt,
            Instant createdAt
    ) {
        notNull(id, "Id must be given");
        notNull(version, "Version must be given");
        notNull(number, "Number must be given");
        notNull(images, "Images must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");
        notNull(notes, "Notes must be given");
        notNull(producedAt, "Produced at date must be given");
        notNull(createdAt, "Created at date must be given");

        return new LookupProduct(
                id,
                version,
                number,
                images,
                links,
                fabricComposition,
                notes,
                producedAt,
                createdAt
        );
    }

}
