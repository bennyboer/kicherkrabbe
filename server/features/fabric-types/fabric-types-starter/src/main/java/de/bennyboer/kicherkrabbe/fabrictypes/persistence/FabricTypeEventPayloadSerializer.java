package de.bennyboer.kicherkrabbe.fabrictypes.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import de.bennyboer.kicherkrabbe.fabrictypes.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.fabrictypes.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.fabrictypes.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.fabrictypes.update.UpdatedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class FabricTypeEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue()
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "name", e.getName().getValue(),
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case UpdatedEvent e -> Map.of(
                    "name", e.getName().getValue()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    FabricTypeName.of((String) payload.get("name"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    FabricTypeName.of((String) payload.get("name")),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "UPDATED" -> UpdatedEvent.of(
                    FabricTypeName.of((String) payload.get("name"))
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
