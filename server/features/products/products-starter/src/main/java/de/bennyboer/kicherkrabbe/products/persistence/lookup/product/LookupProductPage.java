package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupProductPage {

    long total;

    List<LookupProduct> products;

    public static LookupProductPage of(long total, List<LookupProduct> products) {
        check(total >= 0, "Total must be greater or equal to 0");
        notNull(products, "Products must be given");

        return new LookupProductPage(total, products);
    }

}
