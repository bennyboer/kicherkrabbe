package de.bennyboer.kicherkrabbe.offers.api.requests;

import de.bennyboer.kicherkrabbe.offers.api.OffersSortDTO;
import de.bennyboer.kicherkrabbe.offers.api.PriceRangeDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedOffersRequest {

    String searchTerm;

    Set<String> categories;

    Set<String> sizes;

    PriceRangeDTO priceRange;

    OffersSortDTO sort;

    long skip;

    long limit;

}
