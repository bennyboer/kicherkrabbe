package de.bennyboer.kicherkrabbe.products.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
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

    public static ProductId create() {
        return new ProductId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "ProductId(%s)".formatted(value);
    }

}
