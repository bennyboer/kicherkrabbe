package de.bennyboer.kicherkrabbe.notifications.channel.telegram;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TelegramChatId {

    String value;

    public static TelegramChatId of(String value) {
        notNull(value, "Telegram chat ID must be given");
        check(!value.isBlank(), "Telegram chat ID must not be blank");

        return new TelegramChatId(value);
    }

    @Override
    public String toString() {
        return "TelegramChatId(%s)".formatted(value);
    }

}
