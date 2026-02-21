package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedOffer {

    OfferId id;

    OfferTitle title;

    OfferSize size;

    Set<OfferCategoryId> categories;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Pricing pricing;

    Notes notes;

    public static PublishedOffer of(
            OfferId id,
            OfferTitle title,
            OfferSize size,
            Set<OfferCategoryId> categories,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Pricing pricing,
            Notes notes
    ) {
        notNull(id, "Offer ID must be given");
        notNull(title, "Title must be given");
        notNull(size, "Size must be given");
        notNull(categories, "Categories must be given");
        notNull(images, "Images must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");
        notNull(pricing, "Pricing must be given");
        notNull(notes, "Notes must be given");

        return new PublishedOffer(id, title, size, categories, images, links, fabricComposition, pricing, notes);
    }

}
