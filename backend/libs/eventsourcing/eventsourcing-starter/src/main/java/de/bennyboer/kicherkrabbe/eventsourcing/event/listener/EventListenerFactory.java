package de.bennyboer.kicherkrabbe.eventsourcing.event.listener;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@AllArgsConstructor
public class EventListenerFactory {

    private final MessageListenerFactory messageListenerFactory;

    private final JsonMapper jsonMapper;

    public EventListener createEventListenerForAllEvents(
            String name,
            AggregateType aggregateType,
            EventListenerHandler handler
    ) {
        notNull(name, "Name must be given");
        notNull(aggregateType, "Aggregate type must be given");
        notNull(handler, "Handler must be given");

        return createEventListener(name, aggregateType, null, handler);
    }

    public <T> EventListener createEventListenerForAllEvents(
            String name,
            AggregateType aggregateType,
            Class<T> eventType,
            TypedEventListenerHandler<T> handler
    ) {
        notNull(name, "Name must be given");
        notNull(aggregateType, "Aggregate type must be given");
        notNull(eventType, "Event type must be given");
        notNull(handler, "Handler must be given");

        return createEventListener(name, aggregateType, null, toUntypedHandler(eventType, handler));
    }

    public Flux<HandleableEvent> createTransientEventListenerForAllEvents(String name, AggregateType aggregateType) {
        notNull(name, "Name must be given");
        notNull(aggregateType, "Aggregate type must be given");

        return createTransientEventListener(name, aggregateType, null);
    }

    public EventListener createEventListenerForEvent(
            String name,
            AggregateType aggregateType,
            EventName eventName,
            EventListenerHandler handler
    ) {
        notNull(name, "Name must be given");
        notNull(aggregateType, "Aggregate type must be given");
        notNull(eventName, "Event name must be given");
        notNull(handler, "Handler must be given");

        return createEventListener(name, aggregateType, eventName, handler);
    }

    public <T> EventListener createEventListenerForEvent(
            String name,
            AggregateType aggregateType,
            EventName eventName,
            Class<T> eventType,
            TypedEventListenerHandler<T> handler
    ) {
        notNull(name, "Name must be given");
        notNull(aggregateType, "Aggregate type must be given");
        notNull(eventName, "Event name must be given");
        notNull(eventType, "Event type must be given");
        notNull(handler, "Handler must be given");

        return createEventListener(name, aggregateType, eventName, toUntypedHandler(eventType, handler));
    }

    public Flux<HandleableEvent> createTransientEventListenerForEvent(
            String name,
            AggregateType aggregateType,
            EventName eventName
    ) {
        notNull(name, "Name must be given");
        notNull(aggregateType, "Aggregate type must be given");
        notNull(eventName, "Event name must be given");

        return createTransientEventListener(name, aggregateType, eventName);
    }

    private EventListener createEventListener(
            String name,
            AggregateType aggregateType,
            @Nullable EventName eventName,
            EventListenerHandler handler
    ) {
        var exchangeTarget = ExchangeTarget.of(aggregateType.getValue().toLowerCase(Locale.ROOT));
        var routingKey = RoutingKey.ofParts(
                "events",
                Optional.ofNullable(eventName)
                        .map(EventName::getValue)
                        .map(String::toLowerCase)
                        .orElse("*")
        );

        MessageListener messageListener = messageListenerFactory.createListener(
                exchangeTarget,
                routingKey,
                name,
                delivery -> parseMessageToEventWithMetadata(delivery.getBody())
                        .flatMap(handler::handle)
        );

        return new EventListener(messageListener);
    }

    private Flux<HandleableEvent> createTransientEventListener(
            String name,
            AggregateType aggregateType,
            @Nullable EventName eventName
    ) {
        var exchangeTarget = ExchangeTarget.of(aggregateType.getValue().toLowerCase(Locale.ROOT));
        var routingKey = RoutingKey.ofParts(
                "events",
                Optional.ofNullable(eventName)
                        .map(EventName::getValue)
                        .map(String::toLowerCase)
                        .orElse("*")
        );

        return messageListenerFactory.createTransientListener(exchangeTarget, routingKey, name)
                .flatMap(delivery -> parseMessageToEventWithMetadata(delivery.getBody()));
    }

    private Mono<HandleableEvent> parseMessageToEventWithMetadata(byte[] message) {
        return deserializeMessage(message)
                .map(payload -> {
                    Map<String, Object> metadataPayload = (Map<String, Object>) payload.get("metadata");
                    var aggregateId = AggregateId.of((String) metadataPayload.get("aggregateId"));
                    var aggregateType = AggregateType.of((String) metadataPayload.get("aggregateType"));
                    var aggregateVersion = Version.of((Integer) metadataPayload.get("aggregateVersion"));
                    var eventName = EventName.of((String) metadataPayload.get("eventName"));
                    var eventVersion = Version.of((Integer) metadataPayload.get("eventVersion"));
                    var agentId = AgentId.of((String) metadataPayload.get("agentId"));
                    var agentType = AgentType.valueOf((String) metadataPayload.get("agentType"));
                    var agent = Agent.of(agentType, agentId);
                    var date = Instant.parse((String) metadataPayload.get("date"));
                    boolean isSnapshot = (boolean) metadataPayload.get("isSnapshot");

                    var metadata = EventMetadata.of(
                            aggregateId,
                            aggregateType,
                            aggregateVersion,
                            agent,
                            date,
                            isSnapshot
                    );

                    Map<String, Object> eventPayload = (Map<String, Object>) payload.get("event");

                    return HandleableEvent.of(metadata, eventName, eventVersion, eventPayload);
                });
    }

    private Mono<Map<String, Object>> deserializeMessage(byte[] message) {
        try {
            Map<String, Object> result = jsonMapper.readValue(message, Map.class);
            return Mono.just(result);
        } catch (JacksonException e) {
            return Mono.error(e);
        }
    }

    private <T> EventListenerHandler toUntypedHandler(Class<T> eventType, TypedEventListenerHandler<T> handler) {
        return handleableEvent -> {
            T typedEvent = jsonMapper.convertValue(handleableEvent.getEvent(), eventType);
            return handler.handle(handleableEvent.getMetadata(), typedEvent);
        };
    }

}
