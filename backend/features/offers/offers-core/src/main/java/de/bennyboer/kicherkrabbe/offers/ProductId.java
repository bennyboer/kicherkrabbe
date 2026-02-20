package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ProductId {

    String value;

    public static ProductId of(String value) {
        notNull(value, "Product ID must be given");
        check(!value.isBlank(), "Product ID must not be blank");

        return new ProductId(value);
    }

    @Override
    public String toString() {
        return "ProductId(%s)".formatted(value);
    }

}
