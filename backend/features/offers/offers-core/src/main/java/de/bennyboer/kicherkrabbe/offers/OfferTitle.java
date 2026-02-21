package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferTitle {

    String value;

    public static OfferTitle of(String value) {
        notNull(value, "Offer title must be given");
        check(!value.isBlank(), "Offer title must not be blank");

        return new OfferTitle(value);
    }

    @Override
    public String toString() {
        return "OfferTitle(%s)".formatted(value);
    }

}
