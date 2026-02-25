package de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo;

import jakarta.annotation.Nullable;
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

    @Nullable
    Long discountedPriceAmount;

    @Nullable
    String discountedPriceCurrency;

    long effectivePriceAmount;

    List<MongoPriceHistoryEntry> priceHistory;

}
