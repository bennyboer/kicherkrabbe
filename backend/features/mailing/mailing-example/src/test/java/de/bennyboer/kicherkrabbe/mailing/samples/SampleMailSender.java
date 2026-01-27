package de.bennyboer.kicherkrabbe.mailing.samples;

import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import lombok.Builder;

@Builder
public class SampleMailSender {

    @Builder.Default
    private String mail = "sender@example.com";

    public SenderDTO toDTO() {
        var dto = new SenderDTO();
        dto.mail = mail;
        return dto;
    }

}
