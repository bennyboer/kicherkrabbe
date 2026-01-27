package de.bennyboer.kicherkrabbe.products.samples;

import de.bennyboer.kicherkrabbe.products.api.FabricCompositionItemDTO;
import de.bennyboer.kicherkrabbe.products.api.FabricTypeDTO;
import lombok.Builder;

@Builder
public class SampleFabricCompositionItem {

    @Builder.Default
    private FabricTypeDTO fabricType = FabricTypeDTO.COTTON;

    @Builder.Default
    private long percentage = 10000;

    public FabricCompositionItemDTO toDTO() {
        var dto = new FabricCompositionItemDTO();
        dto.fabricType = fabricType;
        dto.percentage = percentage;
        return dto;
    }

}
