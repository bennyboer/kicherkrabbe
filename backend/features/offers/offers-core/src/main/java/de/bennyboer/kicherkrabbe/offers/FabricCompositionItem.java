package de.bennyboer.kicherkrabbe.offers;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricCompositionItem {

    FabricType fabricType;

    LowPrecisionFloat percentage;

    public static FabricCompositionItem of(FabricType fabricType, LowPrecisionFloat percentage) {
        notNull(fabricType, "Fabric type must be given");
        notNull(percentage, "Percentage must be given");

        return new FabricCompositionItem(fabricType, percentage);
    }

    @Override
    public String toString() {
        return "FabricCompositionItem(fabricType=%s, percentage=%s)".formatted(fabricType, percentage);
    }

}
