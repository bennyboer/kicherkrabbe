package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Product {

    ProductId id;

    ProductNumber number;

    public static Product of(ProductId id, ProductNumber number) {
        notNull(id, "Product ID must be given");
        notNull(number, "Product number must be given");

        return new Product(id, number);
    }

}
