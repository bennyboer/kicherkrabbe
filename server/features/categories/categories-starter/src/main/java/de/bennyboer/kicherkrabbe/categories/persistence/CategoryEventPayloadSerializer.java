package de.bennyboer.kicherkrabbe.categories.persistence;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.categories.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.categories.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.categories.regroup.RegroupedEvent;
import de.bennyboer.kicherkrabbe.categories.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.categories.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CategoryEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "group", switch (e.getGroup()) {
                        case CLOTHING -> "CLOTHING";
                        case NONE -> "NONE";
                    }
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "name", e.getName().getValue(),
                        "group", switch (e.getGroup()) {
                            case CLOTHING -> "CLOTHING";
                            case NONE -> "NONE";
                        },
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case RenamedEvent e -> Map.of(
                    "name", e.getName().getValue()
            );
            case RegroupedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "group", switch (e.getGroup()) {
                        case CLOTHING -> "CLOTHING";
                        case NONE -> "NONE";
                    }
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    CategoryName.of((String) payload.get("name")),
                    CategoryGroup.valueOf((String) payload.get("group"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    CategoryName.of((String) payload.get("name")),
                    CategoryGroup.valueOf((String) payload.get("group")),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "RENAMED" -> RenamedEvent.of(
                    CategoryName.of((String) payload.get("name"))
            );
            case "REGROUPED" -> RegroupedEvent.of(
                    CategoryName.of((String) payload.get("name")),
                    CategoryGroup.valueOf((String) payload.get("group"))
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
