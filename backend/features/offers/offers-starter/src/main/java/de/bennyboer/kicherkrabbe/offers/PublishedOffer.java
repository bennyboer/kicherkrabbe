package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedOffer {

    OfferId id;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Pricing pricing;

    Notes notes;

    public static PublishedOffer of(
            OfferId id,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Pricing pricing,
            Notes notes
    ) {
        notNull(id, "Offer ID must be given");
        notNull(images, "Images must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");
        notNull(pricing, "Pricing must be given");
        notNull(notes, "Notes must be given");

        return new PublishedOffer(id, images, links, fabricComposition, pricing, notes);
    }

}
