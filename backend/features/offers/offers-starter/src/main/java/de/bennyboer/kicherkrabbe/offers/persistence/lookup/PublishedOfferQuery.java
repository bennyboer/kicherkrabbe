package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferSize;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishedOfferQuery {

    String searchTerm;

    Set<OfferCategoryId> categories;

    Set<OfferSize> sizes;

    @Nullable
    Long minPrice;

    @Nullable
    Long maxPrice;

    @Nullable
    OfferSortProperty sortProperty;

    @Nullable
    OfferSortDirection sortDirection;

    long skip;

    long limit;

    public static PublishedOfferQuery of(
            String searchTerm,
            Set<OfferCategoryId> categories,
            Set<OfferSize> sizes,
            @Nullable Long minPrice,
            @Nullable Long maxPrice,
            @Nullable OfferSortProperty sortProperty,
            @Nullable OfferSortDirection sortDirection,
            long skip,
            long limit
    ) {
        return new PublishedOfferQuery(searchTerm, categories, sizes, minPrice, maxPrice, sortProperty, sortDirection, skip, limit);
    }

}
