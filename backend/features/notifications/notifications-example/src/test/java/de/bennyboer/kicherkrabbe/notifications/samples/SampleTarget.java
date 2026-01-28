package de.bennyboer.kicherkrabbe.notifications.samples;

import de.bennyboer.kicherkrabbe.notifications.api.TargetDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetTypeDTO;
import lombok.Builder;

@Builder
public class SampleTarget {

    @Builder.Default
    private TargetTypeDTO type = TargetTypeDTO.SYSTEM;

    @Builder.Default
    private String id = "TARGET_ID";

    public TargetDTO toDTO() {
        var dto = new TargetDTO();
        dto.type = type;
        dto.id = id;
        return dto;
    }

}
