package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferCategoryName {

    String value;

    public static OfferCategoryName of(String value) {
        notNull(value, "Offer category name must be given");
        check(!value.isBlank(), "Offer category name must not be blank");

        return new OfferCategoryName(value);
    }

    @Override
    public String toString() {
        return "OfferCategoryName(%s)".formatted(value);
    }

}
