package de.bennyboer.kicherkrabbe.offers.api;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PriceRangeDTO {

    @Nullable
    Long minPrice;

    @Nullable
    Long maxPrice;

}
