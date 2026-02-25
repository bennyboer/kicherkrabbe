package de.bennyboer.kicherkrabbe.offers.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class OffersSortDTO {

    OffersSortPropertyDTO property;

    OffersSortDirectionDTO direction;

}
