package de.bennyboer.kicherkrabbe.messaging.outbox.publisher;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor
public class RabbitOutboxEntryPublisher implements MessagingOutboxEntryPublisher {

    private static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(10);

    private final Set<String> declaredExchanges = ConcurrentHashMap.newKeySet();

    private final RabbitTemplate rabbitTemplate;

    private final RabbitAdmin rabbitAdmin;

    private final JsonMapper jsonMapper;

    @Override
    public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .flatMap(this::toMessage)
                .delayUntil(this::declareExchangeIfNotExists)
                .collectList()
                .filter(messages -> !messages.isEmpty())
                .flatMap(this::sendMessagesWithConfirm);
    }

    private Mono<Void> sendMessagesWithConfirm(List<MessageWithExchange> messages) {
        return Mono.fromCallable(() -> {
                    rabbitTemplate.invoke(operations -> {
                        for (var messageWithExchange : messages) {
                            operations.send(
                                    messageWithExchange.exchange(),
                                    messageWithExchange.routingKey(),
                                    messageWithExchange.message()
                            );
                        }
                        boolean confirmed = operations.waitForConfirms(CONFIRM_TIMEOUT.toMillis());
                        if (!confirmed) {
                            throw new RuntimeException("Publisher confirms not received within timeout");
                        }
                        return null;
                    });
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<Void> declareExchangeIfNotExists(MessageWithExchange messageWithExchange) {
        return Mono.just(messageWithExchange.exchange())
                .filter(exchange -> !declaredExchanges.contains(exchange))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(exchangeToDeclare -> {
                    TopicExchange exchange = ExchangeBuilder.topicExchange(exchangeToDeclare)
                            .durable(true)
                            .build();
                    rabbitAdmin.declareExchange(exchange);
                })
                .doOnNext(declaredExchanges::add)
                .doOnNext(exchange -> log.info("Declared exchange '{}'", exchange))
                .then();
    }

    private Mono<MessageWithExchange> toMessage(MessagingOutboxEntry entry) {
        String exchange = entry.getTarget().getExchange().orElseThrow(() -> new IllegalArgumentException(
                "We currently only support publishing to an exchange target"
        )).getName();
        String routingKey = entry.getRoutingKey().asString();

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setContentEncoding("UTF-8");
        properties.setMessageId(entry.getId().getValue());

        return serializePayload(entry)
                .map(payload -> {
                    Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
                    return new MessageWithExchange(exchange, routingKey, message);
                });
    }

    private Mono<String> serializePayload(MessagingOutboxEntry entry) {
        try {
            return Mono.just(jsonMapper.writeValueAsString(entry.getPayload()));
        } catch (JacksonException e) {
            return Mono.error(e);
        }
    }

    private record MessageWithExchange(String exchange, String routingKey, Message message) {
    }

}
