package de.bennyboer.kicherkrabbe.messaging.listener;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.function.Function;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@AllArgsConstructor
public class RabbitMessageListenerFactory implements MessageListenerFactory {

    private static final int DEFAULT_PREFETCH_COUNT = 10;

    private final ConnectionFactory connectionFactory;

    private final RabbitAdmin rabbitAdmin;

    private final ReactiveTransactionManager transactionManager;

    private final MessagingInbox inbox;

    private final MessageListenerContainerManager containerManager;

    @Override
    public MessageListener createListener(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName,
            Function<Message, Mono<Void>> handler
    ) {
        MessageListenerQueues queues = setupQueuesAndBindings(exchange, routingKey, listenerName);

        return new MessageListener(
                transactionManager,
                inbox,
                () -> createMessageFlux(queues.getNormal()),
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
        return listenTransient(exchange, routingKey, listenerName);
    }

    private Flux<Message> listenTransient(
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

        declareExchangeIfNotExists(exchange);

        Queue queue = QueueBuilder.nonDurable(queueName)
                .autoDelete()
                .withArgument("x-expires", 1_800_000)
                .build();
        rabbitAdmin.declareQueue(queue);

        Binding binding = BindingBuilder.bind(queue)
                .to(new TopicExchange(exchange.getName()))
                .with(routingKey.asString());
        rabbitAdmin.declareBinding(binding);

        return createAutoAckMessageFlux(queueName);
    }

    private MessageListenerQueues setupQueuesAndBindings(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        declareExchangeIfNotExists(exchange);
        return declareAndBindListenerQueuesToExchangeForRoutingKey(exchange, routingKey, listenerName);
    }

    private MessageListenerQueues declareAndBindListenerQueuesToExchangeForRoutingKey(
            ExchangeTarget exchange,
            RoutingKey routingKey,
            String listenerName
    ) {
        String baseName = "%s-%s-%s".formatted(exchange.getName(), routingKey.asString(), listenerName);
        String normalQueueName = baseName + "-normal";
        String deadQueueName = baseName + "-dead";

        Queue deadQueue = QueueBuilder.durable(deadQueueName).build();
        rabbitAdmin.declareQueue(deadQueue);

        Queue normalQueue = QueueBuilder.durable(normalQueueName)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", "",
                        "x-dead-letter-routing-key", deadQueueName
                ))
                .build();
        rabbitAdmin.declareQueue(normalQueue);

        Binding binding = BindingBuilder.bind(normalQueue)
                .to(new TopicExchange(exchange.getName()))
                .with(routingKey.asString());
        rabbitAdmin.declareBinding(binding);

        return MessageListenerQueues.of(normalQueueName, deadQueueName);
    }

    private void declareExchangeIfNotExists(ExchangeTarget exchange) {
        TopicExchange topicExchange = ExchangeBuilder.topicExchange(exchange.getName())
                .durable(true)
                .build();
        rabbitAdmin.declareExchange(topicExchange);
    }

    private Flux<AcknowledgableMessage> createMessageFlux(String queueName) {
        Sinks.Many<AcknowledgableMessage> sink = Sinks.many().unicast().onBackpressureBuffer();

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setPrefetchCount(DEFAULT_PREFETCH_COUNT);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            var ackMessage = new RabbitAcknowledgableMessage(message, channel);
            var result = sink.tryEmitNext(ackMessage);
            if (result.isFailure()) {
                log.warn("Failed to emit message to sink (result: {}), nacking for requeue", result);
                ackMessage.nackSync(true);
            }
        });
        containerManager.register(container);
        container.start();

        return sink.asFlux()
                .doOnCancel(container::stop)
                .doOnTerminate(container::stop);
    }

    private Flux<Message> createAutoAckMessageFlux(String queueName) {
        Sinks.Many<Message> sink = Sinks.many().unicast().onBackpressureBuffer();

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setPrefetchCount(DEFAULT_PREFETCH_COUNT);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setMessageListener(message -> {
            var result = sink.tryEmitNext(message);
            if (result.isFailure()) {
                log.warn("Failed to emit message to sink (result: {}), message lost due to auto-ack", result);
            }
        });
        containerManager.register(container);
        container.start();

        return sink.asFlux()
                .doOnCancel(container::stop)
                .doOnTerminate(container::stop);
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
