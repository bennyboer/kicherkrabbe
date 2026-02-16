package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.listener.AcknowledgableMessage;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

@AllArgsConstructor
public class InMemoryMessageListenerFactory implements MessageListenerFactory {

    private final ReactiveTransactionManager transactionManager;

    private final MessagingInbox inbox;

    private final InMemoryMessageBus messageBus;

    @Override
    public MessageListener createListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName,
            Function<Message, Mono<Void>> handler
    ) {
        Sinks.Many<AcknowledgableMessage> sink = Sinks.many().unicast().onBackpressureBuffer();
        messageBus.register(exchange.getName(), routingKey.asString(), sink);

        return new MessageListener(
                transactionManager,
                inbox,
                sink::asFlux,
                listenerName,
                handler
        );
    }

    @Override
    public Flux<Message> createTransientListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        Sinks.Many<AcknowledgableMessage> sink = Sinks.many().unicast().onBackpressureBuffer();
        messageBus.register(exchange.getName(), routingKey.asString(), sink);

        return sink.asFlux().map(AcknowledgableMessage::getMessage);
    }

}
