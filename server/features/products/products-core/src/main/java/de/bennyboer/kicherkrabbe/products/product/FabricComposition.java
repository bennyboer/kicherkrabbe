package de.bennyboer.kicherkrabbe.products.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricComposition {

    Set<FabricCompositionItem> items;

    public static FabricComposition of(Set<FabricCompositionItem> items) {
        notNull(items, "Items must be given");

        LowPrecisionFloat totalPercentage = items.stream()
                .map(FabricCompositionItem::getPercentage)
                .reduce(LowPrecisionFloat.zero(), LowPrecisionFloat::add);
        check(totalPercentage.equals(LowPrecisionFloat.of(10000)), "Total percentage must be 100");

        return new FabricComposition(items);
    }

}
