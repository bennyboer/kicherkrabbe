package de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.serialization.EventSerializer;
import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Locale;
import java.util.Map;

@AllArgsConstructor
public class MessagingEventPublisher implements EventPublisher {

    private final MessagingOutbox outbox;

    private final EventSerializer serializer;

    private final Clock clock;

    @Override
    public Mono<Void> publish(EventWithMetadata event) {
        MessagingOutboxEntry entry = toEntry(event);

        return outbox.insert(entry);
    }

    private MessagingOutboxEntry toEntry(EventWithMetadata eventWithMetadata) {
        Event event = eventWithMetadata.getEvent();
        EventMetadata metadata = eventWithMetadata.getMetadata();

        String exchangeName = metadata.getAggregateType()
                .getValue()
                .toLowerCase(Locale.ROOT);
        var exchange = ExchangeTarget.of(exchangeName);
        var target = MessageTarget.exchange(exchange);

        String eventName = event.getEventName()
                .getValue()
                .toLowerCase(Locale.ROOT);
        var routingKey = RoutingKey.ofParts("events", eventName);

        Map<String, Object> eventPayload = serializer.serialize(event);
        Map<String, Object> payload = Map.of(
                "event", eventPayload,
                "metadata", Map.of(
                        "aggregateId", metadata.getAggregateId().getValue(),
                        "aggregateType", metadata.getAggregateType().getValue(),
                        "aggregateVersion", metadata.getAggregateVersion().getValue(),
                        "eventName", event.getEventName().getValue(),
                        "eventVersion", event.getVersion().getValue()
                )
        );

        return MessagingOutboxEntry.create(
                target,
                routingKey,
                payload,
                clock
        );
    }

}
