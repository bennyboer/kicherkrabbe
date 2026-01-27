package de.bennyboer.kicherkrabbe.messaging.inbox;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class IncomingMessageId {

    String value;

    public static IncomingMessageId of(String value) {
        notNull(value, "Incoming message ID must be given");
        check(!value.isBlank(), "Incoming message ID must not be empty");

        return new IncomingMessageId(value);
    }

    @Override
    public String toString() {
        return "IncomingMessageId(%s)".formatted(value);
    }

}
