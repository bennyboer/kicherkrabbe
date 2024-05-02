package de.bennyboer.kicherkrabbe.messaging.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

@MessagingTest
public abstract class BaseMessagingTest {

    private final MessageListenerFactory messageListenerFactory;

    private final MessagingOutbox outbox;

    private final ReactiveTransactionManager transactionManager;

    private final ObjectMapper objectMapper;

    public BaseMessagingTest(
            MessageListenerFactory factory,
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager,
            ObjectMapper objectMapper
    ) {
        this.messageListenerFactory = factory;
        this.outbox = outbox;
        this.transactionManager = transactionManager;
        this.objectMapper = objectMapper;
    }

    public void send(String exchange, String routingKey, Map<String, Object> payload) {
        var exchangeTarget = ExchangeTarget.of(exchange);
        var messageTarget = MessageTarget.exchange(exchangeTarget);
        var rKey = RoutingKey.parse(routingKey);
        var entry = MessagingOutboxEntry.create(
                messageTarget,
                rKey,
                payload,
                Clock.systemUTC()
        );
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        outbox.insert(entry)
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Flux<Map<String, Object>> receive(String exchange, String routingKey) {
        return receive(exchange, routingKey, message -> Mono.empty());
    }

    public Flux<Map<String, Object>> receive(
            String exchange,
            String routingKey,
            Function<Map<String, Object>, Mono<Void>> handler
    ) {
        var exchangeTarget = ExchangeTarget.of(exchange);
        var rKey = RoutingKey.parse(routingKey);
        String listenerName = "test-listener";

        messageListenerFactory.setupQueuesAndBindings(exchangeTarget, rKey, listenerName).block();

        return Flux.create(sink -> {
            MessageListener listener = messageListenerFactory.createListener(
                    exchangeTarget,
                    rKey,
                    listenerName,
                    delivery -> {
                        try {
                            Map<String, Object> message = objectMapper.readValue(delivery.getBody(), Map.class);

                            return handler.apply(message)
                                    .then(Mono.fromRunnable(() -> sink.next(message)));
                        } catch (IOException e) {
                            return Mono.error(e);
                        }
                    }
            );

            listener.start();
            sink.onDispose(listener::destroy);
        });
    }

}
