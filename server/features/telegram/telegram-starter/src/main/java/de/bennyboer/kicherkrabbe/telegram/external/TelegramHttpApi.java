package de.bennyboer.kicherkrabbe.telegram.external;

import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatId;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatMessage;
import reactor.core.publisher.Mono;

public class TelegramHttpApi implements TelegramApi {

    @Override
    public Mono<Void> sendMessageViaBot(ChatId chatId, ChatMessage message, ApiToken botApiToken) {
        return Mono.empty(); // TODO
    }

}
