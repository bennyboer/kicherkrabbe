package de.bennyboer.kicherkrabbe.messaging.outbox;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MessagingOutboxEntryLock {

    String value;

    public static MessagingOutboxEntryLock of(String value) {
        notNull(value, "Value must be given");
        check(!value.isBlank(), "Value must not be blank");

        return new MessagingOutboxEntryLock(value);
    }

    public static MessagingOutboxEntryLock create() {
        return new MessagingOutboxEntryLock(java.util.UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "MessagingOutboxEntryLock(%s)".formatted(value);
    }

}
