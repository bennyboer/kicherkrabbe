package de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class SnapshotAwareEventSerializer implements EventSerializer {

    private final EventSerializer delegate;

    public static SnapshotAwareEventSerializer wrap(EventSerializer serializer) {
        if (serializer instanceof SnapshotAwareEventSerializer) {
            return (SnapshotAwareEventSerializer) serializer;
        }
        return new SnapshotAwareEventSerializer(serializer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> serialize(Event event) {
        if (event instanceof SnapshotEvent snapshotEvent) {
            return snapshotEvent.getState();
        }
        return delegate.serialize(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        if (SnapshotEvent.NAME.equals(name)) {
            return SnapshotEvent.of(payload);
        }
        return delegate.deserialize(name, eventVersion, payload);
    }

}
