package de.bennyboer.kicherkrabbe.messaging.outbox;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MessagingOutboxEntryId {

    String value;

    public static MessagingOutboxEntryId of(String value) {
        notNull(value, "Messaging Outbox Entry ID must be given");
        check(!value.isBlank(), "Messaging Outbox Entry ID must not be empty");

        return new MessagingOutboxEntryId(value);
    }

    public static MessagingOutboxEntryId create() {
        return new MessagingOutboxEntryId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "MessagingOutboxEntryId(%s)".formatted(value);
    }

}
