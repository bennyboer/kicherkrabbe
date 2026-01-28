package de.bennyboer.kicherkrabbe.notifications.channel.telegram;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Telegram {

    TelegramChatId chatId;

    public static Telegram of(TelegramChatId chatId) {
        notNull(chatId, "Telegram chat ID must be given");

        return new Telegram(chatId);
    }

    @Override
    public String toString() {
        return "Telegram(chatId=%s)".formatted(chatId);
    }

}
