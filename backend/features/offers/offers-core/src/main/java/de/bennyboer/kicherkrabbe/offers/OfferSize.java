package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferSize {

    String value;

    public static OfferSize of(String value) {
        notNull(value, "Offer size must be given");
        check(!value.isBlank(), "Offer size must not be blank");

        return new OfferSize(value);
    }

    @Override
    public String toString() {
        return "OfferSize(%s)".formatted(value);
    }

}
