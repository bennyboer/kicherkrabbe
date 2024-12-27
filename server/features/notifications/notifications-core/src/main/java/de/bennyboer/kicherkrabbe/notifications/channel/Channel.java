package de.bennyboer.kicherkrabbe.notifications.channel;

import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.Telegram;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.notifications.channel.ChannelType.EMAIL;
import static de.bennyboer.kicherkrabbe.notifications.channel.ChannelType.TELEGRAM;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Channel {

    ChannelType type;

    @Nullable
    EMail mail;

    @Nullable
    Telegram telegram;

    private static Channel empty(ChannelType type) {
        notNull(type, "Channel type must be given");

        return new Channel(type, null, null);
    }

    public static Channel mail(EMail mail) {
        notNull(mail, "Mail must be given");

        return empty(EMAIL).withMail(mail);
    }

    public static Channel telegram(Telegram telegram) {
        notNull(telegram, "Telegram must be given");

        return empty(TELEGRAM).withTelegram(telegram);
    }

    public Optional<EMail> getMail() {
        return Optional.ofNullable(mail);
    }

    public Optional<Telegram> getTelegram() {
        return Optional.ofNullable(telegram);
    }

    @Override
    public String toString() {
        return "Channel(type=%s, mail=%s)".formatted(type, mail);
    }

}
