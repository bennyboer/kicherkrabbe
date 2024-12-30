package de.bennyboer.kicherkrabbe.telegram.external;

import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatId;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatMessage;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TelegramHttpApi implements TelegramApi {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.telegram.org")
            .build();

    @Override
    public Mono<Void> sendMessageViaBot(ChatId chatId, ChatMessage message, ApiToken botApiToken) {
        var path = "/bot%s/sendMessage".formatted(botApiToken.getValue());

        return webClient.post()
                .uri(path)
                .bodyValue(Map.of(
                        "chat_id", chatId.getValue(),
                        "text", message.getValue(),
                        "parse_mode", "Markdown"
                ))
                .retrieve()
                .bodyToMono(Void.class);
    }

}
