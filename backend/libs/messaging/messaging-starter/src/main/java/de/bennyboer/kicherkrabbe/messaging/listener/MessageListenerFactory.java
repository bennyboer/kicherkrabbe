package de.bennyboer.kicherkrabbe.messaging.listener;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import org.springframework.amqp.core.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface MessageListenerFactory {

    MessageListener createListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName,
            Function<Message, Mono<Void>> handler
    );

    Flux<Message> createTransientListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    );

}
