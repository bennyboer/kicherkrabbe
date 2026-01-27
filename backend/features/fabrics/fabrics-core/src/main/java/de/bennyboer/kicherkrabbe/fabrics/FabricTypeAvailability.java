package de.bennyboer.kicherkrabbe.fabrics;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypeAvailability {

    FabricTypeId typeId;

    boolean inStock;

    public static FabricTypeAvailability of(FabricTypeId typeId, boolean inStock) {
        notNull(typeId, "Fabric type ID must be given");

        return new FabricTypeAvailability(typeId, inStock);
    }

}
