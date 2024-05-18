package de.bennyboer.kicherkrabbe.assets.persistence;

import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.assets.FileName;
import de.bennyboer.kicherkrabbe.assets.Location;
import de.bennyboer.kicherkrabbe.assets.LocationType;
import de.bennyboer.kicherkrabbe.assets.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.assets.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.assets.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static de.bennyboer.kicherkrabbe.assets.LocationType.FILE;

public class AssetEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> {
                var result = new HashMap<String, Object>(Map.of("contentType", e.getContentType().getValue()));

                var locationMap = new HashMap<String, Object>(Map.of(
                        "type", e.getLocation().getType().name()
                ));
                e.getLocation().getFileName().ifPresent(filePath -> locationMap.put("filePath", filePath.getValue()));
                result.put("location", locationMap);

                yield result;
            }
            case SnapshottedEvent e -> {
                var result = new HashMap<String, Object>(Map.of(
                        "contentType", e.getContentType().getValue(),
                        "createdAt", e.getCreatedAt().toString()
                ));

                var locationMap = new HashMap<String, Object>(Map.of(
                        "type", e.getLocation().getType().name()
                ));
                e.getLocation().getFileName().ifPresent(filePath -> locationMap.put("filePath", filePath.getValue()));
                result.put("location", locationMap);

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> {
                var contentType = ContentType.of((String) payload.get("contentType"));

                Map<String, Object> locationMap = (Map<String, Object>) payload.get("location");
                String locationTypeStr = locationMap.get("type").toString();
                LocationType type = switch (locationTypeStr) {
                    case "FILE" -> FILE;
                    default -> throw new IllegalStateException("Unexpected location type: " + locationTypeStr);
                };
                Location location = switch (type) {
                    case FILE -> Location.file(FileName.of(locationMap.get("filePath").toString()));
                };

                yield CreatedEvent.of(contentType, location);
            }
            case "SNAPSHOTTED" -> {
                var contentType = ContentType.of((String) payload.get("contentType"));

                Map<String, Object> locationMap = (Map<String, Object>) payload.get("location");
                String locationTypeStr = locationMap.get("type").toString();
                LocationType type = switch (locationTypeStr) {
                    case "FILE" -> FILE;
                    default -> throw new IllegalStateException("Unexpected location type: " + locationTypeStr);
                };
                Location location = switch (type) {
                    case FILE -> Location.file(FileName.of(locationMap.get("filePath").toString()));
                };

                Instant createdAt = Instant.parse((String) payload.get("createdAt"));
                Instant deletedAt = payload.containsKey("deletedAt")
                        ? Instant.parse((String) payload.get("deletedAt"))
                        : null;

                yield SnapshottedEvent.of(contentType, location, createdAt, deletedAt);
            }
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
