package de.bennyboer.kicherkrabbe.telegram.samples;

import de.bennyboer.kicherkrabbe.telegram.api.requests.SendMessageViaBotRequest;
import lombok.Builder;

@Builder
public class SampleTelegramMessage {

    @Builder.Default
    private String chatId = "123456789";

    @Builder.Default
    private String text = "Hello, World!";

    public SendMessageViaBotRequest toRequest() {
        var request = new SendMessageViaBotRequest();
        request.chatId = chatId;
        request.text = text;
        return request;
    }

}
