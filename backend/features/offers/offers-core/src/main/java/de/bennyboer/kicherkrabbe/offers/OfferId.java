package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class OfferId {

    String value;

    public static OfferId of(String value) {
        notNull(value, "Offer ID must be given");
        check(!value.isBlank(), "Offer ID must not be blank");

        return new OfferId(value);
    }

    public static OfferId create() {
        return new OfferId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "OfferId(%s)".formatted(value);
    }

}
