package de.bennyboer.kicherkrabbe.notifications.notification;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Message {

    String value;

    public static Message of(String value) {
        notNull(value, "Message must be given");
        check(!value.isBlank(), "Message must not be blank");

        return new Message(value);
    }

    @Override
    public String toString() {
        return "Message(%s)".formatted(value);
    }

    public Message anonymize() {
        return withValue("ANONYMIZED");
    }

}
