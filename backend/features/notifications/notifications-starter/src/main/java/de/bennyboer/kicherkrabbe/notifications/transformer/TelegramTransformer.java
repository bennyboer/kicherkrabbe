package de.bennyboer.kicherkrabbe.notifications.transformer;

import de.bennyboer.kicherkrabbe.notifications.api.TelegramDTO;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.Telegram;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.TelegramChatId;

public class TelegramTransformer {

    public static TelegramDTO toApi(Telegram telegram) {
        var result = new TelegramDTO();

        result.chatId = telegram.getChatId().getValue();

        return result;
    }

    public static Telegram toInternal(TelegramDTO telegram) {
        return Telegram.of(TelegramChatId.of(telegram.chatId));
    }

}
