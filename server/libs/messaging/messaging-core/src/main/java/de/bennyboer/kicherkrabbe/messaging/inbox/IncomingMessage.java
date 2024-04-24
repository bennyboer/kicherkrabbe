package de.bennyboer.kicherkrabbe.messaging.inbox;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class IncomingMessage {

    IncomingMessageId id;

    Instant receivedAt;

    public static IncomingMessage of(IncomingMessageId id, Instant receivedAt) {
        notNull(id, "Incoming message ID must be given");
        notNull(receivedAt, "Received at must be given");

        return new IncomingMessage(id, receivedAt);
    }

}
