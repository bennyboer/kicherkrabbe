package de.bennyboer.kicherkrabbe.users.internal;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.users.internal.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.users.internal.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.users.internal.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.users.internal.snapshot.SnapshottedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class UserEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "firstName", e.getName().getFirstName().getValue(),
                    "lastName", e.getName().getLastName().getValue(),
                    "mail", e.getMail().getValue()
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "firstName", e.getName().getFirstName().getValue(),
                        "lastName", e.getName().getLastName().getValue(),
                        "mail", e.getMail().getValue(),
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case RenamedEvent e -> Map.of(
                    "firstName", e.getName().getFirstName().getValue(),
                    "lastName", e.getName().getLastName().getValue()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    FullName.of(
                            FirstName.of((String) payload.get("firstName")),
                            LastName.of((String) payload.get("lastName"))
                    ),
                    Mail.of((String) payload.get("mail"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    FullName.of(
                            FirstName.of((String) payload.get("firstName")),
                            LastName.of((String) payload.get("lastName"))
                    ),
                    Mail.of((String) payload.get("mail")),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "RENAMED" -> RenamedEvent.of(
                    FullName.of(
                            FirstName.of((String) payload.get("firstName")),
                            LastName.of((String) payload.get("lastName"))
                    )
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

}
