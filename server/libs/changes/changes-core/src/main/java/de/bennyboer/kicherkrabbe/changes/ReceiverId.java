package de.bennyboer.kicherkrabbe.changes;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * The ID of the receiver. Most likely this would be some kind of user ID.
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class ReceiverId {

    String value;

    public static ReceiverId of(String value) {
        notNull(value, "Receiver ID must be given");
        check(!value.isBlank(), "Receiver ID must not be blank");

        return new ReceiverId(value);
    }

    @Override
    public String toString() {
        return "ReceiverId(%s)".formatted(value);
    }

}
