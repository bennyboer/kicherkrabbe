package de.bennyboer.kicherkrabbe.mailing.samples;

import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import lombok.Builder;

@Builder
public class SampleMailReceiver {

    @Builder.Default
    private String mail = "receiver@example.com";

    public ReceiverDTO toDTO() {
        var dto = new ReceiverDTO();
        dto.mail = mail;
        return dto;
    }

}
