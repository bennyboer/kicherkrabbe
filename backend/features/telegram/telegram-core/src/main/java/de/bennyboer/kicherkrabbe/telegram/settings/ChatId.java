package de.bennyboer.kicherkrabbe.telegram.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ChatId {

    String value;

    public static ChatId of(String value) {
        notNull(value, "Chat ID must be given");
        check(!value.isBlank(), "Chat ID must not be blank");

        return new ChatId(value);
    }

    @Override
    public String toString() {
        return "ChatId(%s)".formatted(value);
    }

}
