package de.bennyboer.kicherkrabbe.telegram.external;

import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatId;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatMessage;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingTelegramApi implements TelegramApi {

    private final List<MessageSentViaBot> messagesSentViaBot = new ArrayList<>();

    @Override
    public Mono<Void> sendMessageViaBot(ChatId chatId, ChatMessage message, ApiToken botApiToken) {
        return Mono.fromSupplier(() -> {
            var messageSentViaBot = MessageSentViaBot.of(chatId, message, botApiToken);
            messagesSentViaBot.add(messageSentViaBot);
            return null;
        });
    }

    public List<MessageSentViaBot> getMessagesSentViaBot() {
        return Collections.unmodifiableList(messagesSentViaBot);
    }

    public void reset() {
        messagesSentViaBot.clear();
    }

}
