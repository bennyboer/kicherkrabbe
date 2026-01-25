package de.bennyboer.kicherkrabbe.mailbox.samples;

import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import lombok.Builder;

@Builder
public class SampleMailboxSender {

    @Builder.Default
    private String name = "John Doe";

    @Builder.Default
    private String mail = "john.doe@example.com";

    @Builder.Default
    private String phone = "+49 123 456789";

    public SenderDTO toDTO() {
        var dto = new SenderDTO();
        dto.name = name;
        dto.mail = mail;
        dto.phone = phone;
        return dto;
    }

}
