package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.MessagingOutboxEntryPublisher;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

@AllArgsConstructor
public class InMemoryOutboxEntryPublisher implements MessagingOutboxEntryPublisher {

    private final InMemoryMessageBus messageBus;

    private final JsonMapper jsonMapper;

    @Override
    public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
        return Flux.fromIterable(entries)
                .flatMap(this::publishEntry)
                .then();
    }

    private Mono<Void> publishEntry(MessagingOutboxEntry entry) {
        String exchange = entry.getTarget().getExchange().orElseThrow(() -> new IllegalArgumentException(
                "We currently only support publishing to an exchange target"
        )).getName();
        String routingKey = entry.getRoutingKey().asString();

        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setContentEncoding("UTF-8");
        properties.setMessageId(entry.getId().getValue());

        return serializePayload(entry)
                .doOnNext(payload -> {
                    Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
                    messageBus.publish(exchange, routingKey, message);
                })
                .then();
    }

    private Mono<String> serializePayload(MessagingOutboxEntry entry) {
        try {
            return Mono.just(jsonMapper.writeValueAsString(entry.getPayload()));
        } catch (JacksonException e) {
            return Mono.error(e);
        }
    }

}
