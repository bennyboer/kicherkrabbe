package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.FabricCompositionItemDTO;
import de.bennyboer.kicherkrabbe.products.product.FabricCompositionItem;
import de.bennyboer.kicherkrabbe.products.product.FabricType;
import de.bennyboer.kicherkrabbe.products.product.LowPrecisionFloat;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class FabricCompositionItemTransformer {

    public static Set<FabricCompositionItem> toInternal(Collection<FabricCompositionItemDTO> items) {
        notNull(items, "FabricComposition items must be given");

        return items.stream()
                .map(FabricCompositionItemTransformer::toInternal)
                .collect(Collectors.toSet());
    }

    public static FabricCompositionItem toInternal(FabricCompositionItemDTO item) {
        notNull(item, "FabricComposition item must be given");

        FabricType fabricType = FabricTypeTransformer.toInternal(item.fabricType);
        LowPrecisionFloat percentage = LowPrecisionFloat.of(item.percentage);

        return FabricCompositionItem.of(fabricType, percentage);
    }

    public static List<FabricCompositionItemDTO> toApi(Set<FabricCompositionItem> items) {
        return items.stream()
                .map(FabricCompositionItemTransformer::toApi)
                .collect(Collectors.toList());
    }

    public static FabricCompositionItemDTO toApi(FabricCompositionItem item) {
        var result = new FabricCompositionItemDTO();

        result.fabricType = FabricTypeTransformer.toApi(item.getFabricType());
        result.percentage = item.getPercentage().getValue();

        return result;
    }

}
