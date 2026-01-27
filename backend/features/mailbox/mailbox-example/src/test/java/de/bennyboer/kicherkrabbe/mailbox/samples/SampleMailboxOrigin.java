package de.bennyboer.kicherkrabbe.mailbox.samples;

import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import lombok.Builder;

@Builder
public class SampleMailboxOrigin {

    @Builder.Default
    private OriginTypeDTO type = OriginTypeDTO.INQUIRY;

    @Builder.Default
    private String id = "ORIGIN_ID";

    public OriginDTO toDTO() {
        var dto = new OriginDTO();
        dto.type = type;
        dto.id = id;
        return dto;
    }

}
