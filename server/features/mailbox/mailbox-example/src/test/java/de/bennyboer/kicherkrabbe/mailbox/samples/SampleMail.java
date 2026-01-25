package de.bennyboer.kicherkrabbe.mailbox.samples;

import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import lombok.Builder;

@Builder
public class SampleMail {

    @Builder.Default
    private SampleMailboxOrigin origin = SampleMailboxOrigin.builder().build();

    @Builder.Default
    private SampleMailboxSender sender = SampleMailboxSender.builder().build();

    @Builder.Default
    private String subject = "Sample Subject";

    @Builder.Default
    private String content = "Sample mail content";

    public ReceiveMailRequest toRequest() {
        var request = new ReceiveMailRequest();
        request.origin = origin.toDTO();
        request.sender = sender.toDTO();
        request.subject = subject;
        request.content = content;
        return request;
    }

}
