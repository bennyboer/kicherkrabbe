package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Message {

    String value;

    public static Message of(String value) {
        notNull(value, "Message must be given");
        check(!value.isBlank(), "Message must not be blank");
        check(value.length() <= 10000, "Message must not exceed 10000 characters");

        return new Message(value);
    }

    public Message anonymize() {
        return Message.of("ANONYMIZED");
    }

    @Override
    public String toString() {
        return "Message(%s)".formatted(value);
    }

}
