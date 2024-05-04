package de.bennyboer.kicherkrabbe.fabrics.aggregate;

import de.bennyboer.kicherkrabbe.fabrics.types.FabricType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypeAvailability {

    FabricType type;

    boolean inStock;

    public static FabricTypeAvailability of(FabricType type, boolean inStock) {
        notNull(type, "Fabric type must be given");

        return new FabricTypeAvailability(type, inStock);
    }

}
