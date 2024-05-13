package de.bennyboer.kicherkrabbe.colors.persistence;

import de.bennyboer.kicherkrabbe.colors.ColorName;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.colors.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.colors.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.colors.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.colors.update.UpdatedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ColorEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "red", e.getRed(),
                    "green", e.getGreen(),
                    "blue", e.getBlue()
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "name", e.getName().getValue(),
                        "red", e.getRed(),
                        "green", e.getGreen(),
                        "blue", e.getBlue(),
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case UpdatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "red", e.getRed(),
                    "green", e.getGreen(),
                    "blue", e.getBlue()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    ColorName.of((String) payload.get("name")),
                    (int) payload.get("red"),
                    (int) payload.get("green"),
                    (int) payload.get("blue")
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    ColorName.of((String) payload.get("name")),
                    (int) payload.get("red"),
                    (int) payload.get("green"),
                    (int) payload.get("blue"),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "UPDATED" -> UpdatedEvent.of(
                    ColorName.of((String) payload.get("name")),
                    (int) payload.get("red"),
                    (int) payload.get("green"),
                    (int) payload.get("blue")
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
