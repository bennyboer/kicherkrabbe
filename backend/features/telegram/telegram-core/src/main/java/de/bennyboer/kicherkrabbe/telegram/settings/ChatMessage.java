package de.bennyboer.kicherkrabbe.telegram.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ChatMessage {

    String value;

    public static ChatMessage of(String value) {
        notNull(value, "Chat message must be given");
        check(!value.isBlank(), "Chat message must not be blank");

        return new ChatMessage(value);
    }

    @Override
    public String toString() {
        return "ChatMessageId(%s)".formatted(value);
    }

}
