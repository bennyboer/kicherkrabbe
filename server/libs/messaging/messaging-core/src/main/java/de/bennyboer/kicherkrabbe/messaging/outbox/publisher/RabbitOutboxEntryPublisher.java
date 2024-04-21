package de.bennyboer.kicherkrabbe.messaging.outbox.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static reactor.rabbitmq.ExchangeSpecification.exchange;

@Slf4j
@AllArgsConstructor
public class RabbitOutboxEntryPublisher implements MessagingOutboxEntryPublisher {

    private final Set<String> declaredExchanges = ConcurrentHashMap.newKeySet();

    private final Sender sender;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .flatMap(this::toOutboundMessage)
                .delayUntil(this::declareExchangeIfNotExists)
                .collectList()
                .flatMap(messages -> sender.sendWithPublishConfirms(Flux.fromIterable(messages)).then());
    }

    private Mono<Void> declareExchangeIfNotExists(OutboundMessage outboundMessage) {
        return Mono.just(outboundMessage.getExchange())
                .filter(exchange -> !declaredExchanges.contains(exchange))
                .delayUntil(exchangeToDeclare -> sender.declareExchange(exchange(exchangeToDeclare)
                        .type("topic")
                        .durable(true)))
                .doOnNext(declaredExchanges::add)
                .doOnNext(exchange -> log.info("Declared exchange '{}'", exchange))
                .then();
    }

    private Mono<OutboundMessage> toOutboundMessage(MessagingOutboxEntry entry) {
        String exchange = entry.getTarget().getExchange().orElseThrow(() -> new IllegalArgumentException(
                "We currently only support publishing to an exchange target"
        )).getName();
        String routingKey = entry.getRoutingKey().asString();

        return serializePayload(entry)
                .map(payload -> new OutboundMessage(
                        exchange,
                        routingKey,
                        payload.getBytes(StandardCharsets.UTF_8)
                ));
    }

    private Mono<String> serializePayload(MessagingOutboxEntry entry) {
        try {
            return Mono.just(objectMapper.writeValueAsString(entry.getPayload()));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

}
