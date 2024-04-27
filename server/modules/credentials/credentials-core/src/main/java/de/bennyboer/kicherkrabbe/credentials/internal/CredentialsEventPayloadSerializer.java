package de.bennyboer.kicherkrabbe.credentials.internal;

import de.bennyboer.kicherkrabbe.credentials.internal.events.*;
import de.bennyboer.kicherkrabbe.credentials.internal.password.EncodedPassword;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CredentialsEventPayloadSerializer implements EventSerializer {

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

                e.getLastUsedAt().ifPresent(lastUsedAt -> result.put("lastUsedAt", lastUsedAt.toString()));
                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case UsageSucceededEvent e -> Map.of(
                    "date", e.getDate().toString()
            );
            case UsageFailedEvent e -> Map.of(
                    "date", e.getDate().toString()
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
                    payload.containsKey("lastUsedAt") ? Instant.parse((String) payload.get("lastUsedAt")) : null,
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "USAGE_SUCCEEDED" -> UsageSucceededEvent.of(Instant.parse((String) payload.get("date")));
            case "USAGE_FAILED" -> UsageFailedEvent.of(Instant.parse((String) payload.get("date")));
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
