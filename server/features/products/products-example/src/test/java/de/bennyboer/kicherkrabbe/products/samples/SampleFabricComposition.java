package de.bennyboer.kicherkrabbe.products.samples;

import de.bennyboer.kicherkrabbe.products.api.FabricCompositionDTO;
import de.bennyboer.kicherkrabbe.products.api.FabricTypeDTO;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class SampleFabricComposition {

    @Singular
    private List<SampleFabricCompositionItem> items;

    public FabricCompositionDTO toDTO() {
        var dto = new FabricCompositionDTO();
        dto.items = items.isEmpty()
                ? List.of(
                        SampleFabricCompositionItem.builder()
                                .fabricType(FabricTypeDTO.COTTON)
                                .percentage(8000)
                                .build()
                                .toDTO(),
                        SampleFabricCompositionItem.builder()
                                .fabricType(FabricTypeDTO.POLYESTER)
                                .percentage(2000)
                                .build()
                                .toDTO()
                )
                : items.stream()
                        .map(SampleFabricCompositionItem::toDTO)
                        .collect(Collectors.toList());
        return dto;
    }

}
