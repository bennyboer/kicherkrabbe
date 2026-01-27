package de.bennyboer.kicherkrabbe.inquiries.samples;

import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import lombok.Builder;

import java.util.UUID;

@Builder
public class SampleInquiry {

    @Builder.Default
    private String requestId = UUID.randomUUID().toString();

    @Builder.Default
    private SampleSender sender = SampleSender.builder().build();

    @Builder.Default
    private String subject = "Sample Subject";

    @Builder.Default
    private String message = "Sample message content";

    public String getRequestId() {
        return requestId;
    }

    public SenderDTO getSenderDTO() {
        return sender.toDTO();
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

}
