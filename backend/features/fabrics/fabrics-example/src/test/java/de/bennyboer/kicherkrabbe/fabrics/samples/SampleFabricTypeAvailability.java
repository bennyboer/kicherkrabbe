package de.bennyboer.kicherkrabbe.fabrics.samples;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeAvailability;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import lombok.Builder;

@Builder
public class SampleFabricTypeAvailability {

    @Builder.Default
    private String typeId = "JERSEY_ID";

    @Builder.Default
    private boolean inStock = true;

    public FabricTypeAvailabilityDTO toDTO() {
        var dto = new FabricTypeAvailabilityDTO();
        dto.typeId = typeId;
        dto.inStock = inStock;
        return dto;
    }

    public FabricTypeAvailability toValue() {
        return FabricTypeAvailability.of(FabricTypeId.of(typeId), inStock);
    }

}
