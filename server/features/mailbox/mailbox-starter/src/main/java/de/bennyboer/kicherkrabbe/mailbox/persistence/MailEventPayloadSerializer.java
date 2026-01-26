package de.bennyboer.kicherkrabbe.mailbox.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailbox.mail.*;
import de.bennyboer.kicherkrabbe.mailbox.mail.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.read.MarkedAsReadEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.receive.ReceivedEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.unread.MarkedAsUnreadEvent;

import java.util.HashMap;
import java.util.Map;

public class MailEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case ReceivedEvent e -> Map.of(
                    "origin", serializeOrigin(e.getOrigin()),
                    "sender", serializeSender(e.getSender()),
                    "subject", e.getSubject().getValue(),
                    "content", e.getContent().getValue()
            );
            case MarkedAsReadEvent ignored -> Map.of();
            case MarkedAsUnreadEvent ignored -> Map.of();
            case DeletedEvent e -> Map.of("origin", serializeOrigin(e.getOrigin()));
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "RECEIVED" -> ReceivedEvent.of(
                    deserializeOrigin((Map<String, Object>) payload.get("origin")),
                    deserializeSender((Map<String, Object>) payload.get("sender")),
                    Subject.of((String) payload.get("subject")),
                    Content.of((String) payload.get("content"))
            );
            case "MARKED_AS_READ" -> MarkedAsReadEvent.of();
            case "MARKED_AS_UNREAD" -> MarkedAsUnreadEvent.of();
            case "DELETED" -> DeletedEvent.of(
                    deserializeOrigin((Map<String, Object>) payload.get("origin"))
            );
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeSender(Sender sender) {
        var result = new HashMap<String, Object>();

        result.put("name", sender.getName().getValue());
        result.put("mail", sender.getMail().getValue());
        sender.getPhone().ifPresent(phone -> result.put("phone", phone.getValue()));

        return result;
    }

    private Sender deserializeSender(Map<String, Object> payload) {
        var name = SenderName.of((String) payload.get("name"));
        var mail = EMail.of((String) payload.get("mail"));
        var phone = payload.containsKey("phone") ? PhoneNumber.of((String) payload.get("phone")) : null;

        return Sender.of(name, mail, phone);
    }

    private Map<String, Object> serializeOrigin(Origin origin) {
        return Map.of(
                "type", origin.getType().name(),
                "id", origin.getId().getValue()
        );
    }

    private Origin deserializeOrigin(Map<String, Object> payload) {
        var type = OriginType.valueOf((String) payload.get("type"));
        var id = OriginId.of((String) payload.get("id"));

        return Origin.of(type, id);
    }

}
