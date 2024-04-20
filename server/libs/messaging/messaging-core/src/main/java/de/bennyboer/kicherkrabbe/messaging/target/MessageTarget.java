package de.bennyboer.kicherkrabbe.messaging.target;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.messaging.target.MessageTargetType.EXCHANGE;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MessageTarget {

    MessageTargetType type;

    @Nullable
    ExchangeTarget exchange;

    public static MessageTarget exchange(ExchangeTarget exchange) {
        notNull(exchange, "Exchange must be given");

        return new MessageTarget(EXCHANGE, exchange);
    }

    public Optional<ExchangeTarget> getExchange() {
        return Optional.ofNullable(exchange);
    }

}
