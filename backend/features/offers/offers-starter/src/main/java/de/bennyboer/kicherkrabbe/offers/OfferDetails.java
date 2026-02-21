package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferDetails {

    OfferId id;

    Version version;

    OfferTitle title;

    OfferSize size;

    Set<OfferCategoryId> categories;

    Product product;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Pricing pricing;

    Notes notes;

    boolean published;

    boolean reserved;

    Instant createdAt;

    @Nullable
    Instant archivedAt;

    public static OfferDetails of(
            OfferId id,
            Version version,
            OfferTitle title,
            OfferSize size,
            Set<OfferCategoryId> categories,
            Product product,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Pricing pricing,
            Notes notes,
            boolean published,
            boolean reserved,
            Instant createdAt,
            @Nullable Instant archivedAt
    ) {
        notNull(id, "Offer ID must be given");
        notNull(version, "Version must be given");
        notNull(title, "Title must be given");
        notNull(size, "Size must be given");
        notNull(categories, "Categories must be given");
        notNull(product, "Product must be given");
        notNull(images, "Images must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");
        notNull(pricing, "Pricing must be given");
        notNull(notes, "Notes must be given");
        notNull(createdAt, "Created at must be given");

        return new OfferDetails(id, version, title, size, categories, product, images, links, fabricComposition, pricing, notes, published, reserved, createdAt, archivedAt);
    }

    public Optional<Instant> getArchivedAt() {
        return Optional.ofNullable(archivedAt);
    }

}
