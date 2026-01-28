package de.bennyboer.kicherkrabbe.users.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.users.FirstName;
import de.bennyboer.kicherkrabbe.users.FullName;
import de.bennyboer.kicherkrabbe.users.LastName;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.users.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.users.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.users.rename.RenamedEvent;

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
