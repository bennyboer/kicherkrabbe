package de.bennyboer.kicherkrabbe.mailing.samples;

import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleMail {

    @Builder.Default
    private SampleMailSender sender = SampleMailSender.builder().build();

    @Singular
    private List<SampleMailReceiver> receivers;

    @Builder.Default
    private String subject = "Sample Subject";

    @Builder.Default
    private String text = "Sample mail text content";

    public SendMailRequest toRequest() {
        var request = new SendMailRequest();
        request.sender = sender.toDTO();
        request.receivers = getReceivers().stream()
                .map(SampleMailReceiver::toDTO)
                .collect(Collectors.toSet());
        request.subject = subject;
        request.text = text;
        return request;
    }

    private List<SampleMailReceiver> getReceivers() {
        if (receivers.isEmpty()) {
            return List.of(SampleMailReceiver.builder().build());
        }
        return receivers;
    }

}
