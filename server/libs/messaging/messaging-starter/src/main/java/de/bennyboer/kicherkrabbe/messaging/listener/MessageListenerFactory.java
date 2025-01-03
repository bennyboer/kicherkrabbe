package de.bennyboer.kicherkrabbe.messaging.listener;

import com.rabbitmq.client.Delivery;
import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.Map;
import java.util.function.Function;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;
import static reactor.rabbitmq.BindingSpecification.queueBinding;
import static reactor.rabbitmq.ExchangeSpecification.exchange;
import static reactor.rabbitmq.QueueSpecification.queue;

@AllArgsConstructor
public class MessageListenerFactory {

    private final Sender sender;

    private final Receiver receiver;

    private final ReactiveTransactionManager transactionManager;

    private final MessagingInbox inbox;

    public MessageListener createListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName,
            Function<Delivery, Mono<Void>> handler
    ) {
        /*
         * Setting up the queues and bindings is done in a blocking manner because the queues must
         * be ready after all beans are initialized.
         * Otherwise we have no way of knowing when to start publishing messages.
         */
        MessageListenerQueues queues = setupQueuesAndBindings(exchange, routingKey, listenerName).block();

        return new MessageListener(
                transactionManager,
                inbox,
                () -> receiver.consumeManualAck(queues.getNormal()),
                listenerName,
                handler
        );
    }

    public Flux<Delivery> createTransientListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        return listenTransient(exchange, routingKey, listenerName);
    }

    private Flux<Delivery> listenTransient(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        String queueName = "%s-%s-%s-%s".formatted(
                exchange.getName(),
                routingKey.asString(),
                listenerName,
                randomUUID()
        );

        return declareExchangeIfNotExists(exchange)
                .then(sender.declareQueue(queue(queueName)
                        .durable(false)
                        .autoDelete(true)
                        .arguments(Map.of("x-expires", 1_800_000))))
                .then(sender.bind(queueBinding(
                        exchange.getName(),
                        routingKey.asString(),
                        queueName
                )))
                .flatMapMany(declaredQueue -> receiver.consumeAutoAck(queueName));
    }

    public Mono<MessageListenerQueues> setupQueuesAndBindings(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        return declareExchangeIfNotExists(exchange)
                .then(declareAndBindListenerQueuesToExchangeForRoutingKey(exchange, routingKey, listenerName));
    }

    private Mono<MessageListenerQueues> declareAndBindListenerQueuesToExchangeForRoutingKey(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        String baseName = "%s-%s-%s".formatted(exchange.getName(), routingKey.asString(), listenerName);
        String normalQueueName = baseName + "-normal";
        String deadQueueName = baseName + "-dead";

        var declareNormalQueue$ = sender.declareQueue(queue(normalQueueName)
                .durable(true)
                .arguments(Map.of(
                        "x-dead-letter-exchange", "",
                        "x-dead-letter-routing-key", deadQueueName
                )));

        var declareDeadQueue$ = sender.declareQueue(queue(deadQueueName)
                .durable(true));

        return Mono.zip(declareNormalQueue$, declareDeadQueue$)
                .delayUntil(tuple -> sender.bind(queueBinding(
                        exchange.getName(),
                        routingKey.asString(),
                        normalQueueName
                )))
                .thenReturn(MessageListenerQueues.of(normalQueueName, deadQueueName));
    }

    private Mono<Void> declareExchangeIfNotExists(ExchangeTarget exchange) {
        return sender.declareExchange(exchange(exchange.getName())
                        .type("topic")
                        .durable(true))
                .then();
    }

    @Value
    @AllArgsConstructor(access = PRIVATE)
    public static class MessageListenerQueues {

        String normal;

        String dead;

        public static MessageListenerQueues of(String normal, String dead) {
            notNull(normal, "Normal queue name must be given");
            notNull(dead, "Dead queue name must be given");
            check(!normal.isBlank(), "Normal queue name must not be empty");
            check(!dead.isBlank(), "Dead queue name must not be empty");

            return new MessageListenerQueues(normal, dead);
        }

    }

}
