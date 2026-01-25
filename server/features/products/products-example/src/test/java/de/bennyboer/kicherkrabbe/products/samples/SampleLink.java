package de.bennyboer.kicherkrabbe.products.samples;

import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import lombok.Builder;

@Builder
public class SampleLink {

    @Builder.Default
    private LinkTypeDTO type = LinkTypeDTO.PATTERN;

    @Builder.Default
    private String id = "PATTERN_ID";

    @Builder.Default
    private String name = "Pattern";

    public LinkDTO toDTO() {
        var dto = new LinkDTO();
        dto.type = type;
        dto.id = id;
        dto.name = name;
        return dto;
    }

}
