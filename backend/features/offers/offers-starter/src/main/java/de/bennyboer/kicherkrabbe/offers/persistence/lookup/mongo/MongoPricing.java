package de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoPricing {

    long priceAmount;

    String priceCurrency;

    Long discountedPriceAmount;

    String discountedPriceCurrency;

    List<MongoPriceHistoryEntry> priceHistory;

}
