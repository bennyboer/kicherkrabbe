package de.bennyboer.kicherkrabbe.telegram.external;

import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatId;
import de.bennyboer.kicherkrabbe.telegram.settings.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MessageSentViaBot {

    ChatId chatId;

    ChatMessage message;

    ApiToken botApiToken;

    public static MessageSentViaBot of(ChatId chatId, ChatMessage message, ApiToken botApiToken) {
        notNull(chatId, "Chat ID must be given");
        notNull(message, "Message must be given");
        notNull(botApiToken, "Bot API token must be given");

        return new MessageSentViaBot(chatId, message, botApiToken);
    }

}
