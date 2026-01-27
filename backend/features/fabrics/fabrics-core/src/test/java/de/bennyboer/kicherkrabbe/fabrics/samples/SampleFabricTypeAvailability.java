package de.bennyboer.kicherkrabbe.fabrics.samples;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeAvailability;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import lombok.Builder;

@Builder
public class SampleFabricTypeAvailability {

    @Builder.Default
    private String typeId = "FABRIC_TYPE_ID";

    @Builder.Default
    private boolean inStock = true;

    public FabricTypeAvailability toValue() {
        return FabricTypeAvailability.of(FabricTypeId.of(typeId), inStock);
    }

}
