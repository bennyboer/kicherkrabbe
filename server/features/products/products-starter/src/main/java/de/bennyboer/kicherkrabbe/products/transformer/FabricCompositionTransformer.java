package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.FabricCompositionDTO;
import de.bennyboer.kicherkrabbe.products.product.FabricComposition;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class FabricCompositionTransformer {

    public static FabricComposition toInternal(FabricCompositionDTO fabricComposition) {
        notNull(fabricComposition, "FabricComposition must be given");

        return FabricComposition.of(FabricCompositionItemTransformer.toInternal(fabricComposition.items));
    }

    public static FabricCompositionDTO toApi(FabricComposition fabricComposition) {
        var result = new FabricCompositionDTO();

        result.items = FabricCompositionItemTransformer.toApi(fabricComposition.getItems());

        return result;
    }

}
