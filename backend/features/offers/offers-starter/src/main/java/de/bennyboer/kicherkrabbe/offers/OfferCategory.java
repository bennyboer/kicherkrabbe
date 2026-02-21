package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferCategory {

    OfferCategoryId id;

    OfferCategoryName name;

    public static OfferCategory of(OfferCategoryId id, OfferCategoryName name) {
        notNull(id, "Offer category id must be given");
        notNull(name, "Offer category name must be given");

        return new OfferCategory(id, name);
    }

}
