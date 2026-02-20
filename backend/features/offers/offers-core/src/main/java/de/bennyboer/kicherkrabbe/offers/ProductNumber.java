package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ProductNumber {

    String value;

    public static ProductNumber of(String value) {
        notNull(value, "Product number must be given");
        check(!value.isBlank(), "Product number must not be blank");

        return new ProductNumber(value);
    }

    @Override
    public String toString() {
        return "ProductNumber(%s)".formatted(value);
    }

}
