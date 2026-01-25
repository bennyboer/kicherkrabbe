package de.bennyboer.kicherkrabbe.products.product.samples;

import de.bennyboer.kicherkrabbe.products.product.FabricCompositionItem;
import de.bennyboer.kicherkrabbe.products.product.FabricType;
import de.bennyboer.kicherkrabbe.products.product.LowPrecisionFloat;
import lombok.Builder;

@Builder
public class SampleFabricCompositionItem {

    @Builder.Default
    private FabricType fabricType = FabricType.COTTON;

    @Builder.Default
    private int percentage = 10000;

    public FabricCompositionItem toValue() {
        return FabricCompositionItem.of(fabricType, LowPrecisionFloat.of(percentage));
    }

}
