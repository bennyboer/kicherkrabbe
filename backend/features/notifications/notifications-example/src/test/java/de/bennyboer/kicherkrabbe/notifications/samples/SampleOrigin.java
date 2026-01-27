package de.bennyboer.kicherkrabbe.notifications.samples;

import de.bennyboer.kicherkrabbe.notifications.api.OriginDTO;
import de.bennyboer.kicherkrabbe.notifications.api.OriginTypeDTO;
import lombok.Builder;

@Builder
public class SampleOrigin {

    @Builder.Default
    private OriginTypeDTO type = OriginTypeDTO.MAIL;

    @Builder.Default
    private String id = "ORIGIN_ID";

    public OriginDTO toDTO() {
        var dto = new OriginDTO();
        dto.type = type;
        dto.id = id;
        return dto;
    }

}
