package de.bennyboer.kicherkrabbe.products.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementedEvent;
import de.bennyboer.kicherkrabbe.products.counter.init.InitEvent;

import java.util.Map;

public class CounterEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case InitEvent ignored -> Map.of();
            case IncrementedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "INITIALIZED" -> InitEvent.of();
            case "INCREMENTED" -> IncrementedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
