package de.bennyboer.kicherkrabbe.auth.internal.credentials;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.events.*;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.password.EncodedPassword;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.mongo.MongoEventPayloadSerializer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CredentialsEventPayloadSerializer implements MongoEventPayloadSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "encodedPassword", e.getEncodedPassword().getValue(),
                    "userId", e.getUserId().getValue()
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "name", e.getName().getValue(),
                        "encodedPassword", e.getEncodedPassword().getValue(),
                        "userId", e.getUserId().getValue(),
                        "failedUsageAttempts", e.getFailedUsageAttempts()
                ));

                e.getLastUsedAt().ifPresent(lastUsedAt -> result.put("lastUsedAt", lastUsedAt));
                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt));

                yield result;
            }
            case UsageSucceededEvent e -> Map.of(
                    "date", e.getDate()
            );
            case UsageFailedEvent e -> Map.of(
                    "date", e.getDate()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    Name.of((String) payload.get("name")),
                    EncodedPassword.of((String) payload.get("encodedPassword")),
                    UserId.of((String) payload.get("userId"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    Name.of((String) payload.get("name")),
                    EncodedPassword.of((String) payload.get("encodedPassword")),
                    UserId.of((String) payload.get("userId")),
                    (int) payload.get("failedUsageAttempts"),
                    payload.containsKey("lastUsedAt") ? (Instant) payload.get("lastUsedAt") : null,
                    payload.containsKey("deletedAt") ? (Instant) payload.get("deletedAt") : null
            );
            case "USAGE_SUCCEEDED" -> UsageSucceededEvent.of((Instant) payload.get("date"));
            case "USAGE_FAILED" -> UsageFailedEvent.of((Instant) payload.get("date"));
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
