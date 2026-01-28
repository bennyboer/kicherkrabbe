package de.bennyboer.kicherkrabbe.inquiries.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.inquiries.*;
import de.bennyboer.kicherkrabbe.inquiries.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.inquiries.send.SentEvent;

import java.util.HashMap;
import java.util.Map;

public class InquiryEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case SentEvent e -> Map.of(
                    "requestId", e.getRequestId().getValue(),
                    "sender", serializeSender(e.getSender()),
                    "subject", e.getSubject().getValue(),
                    "message", e.getMessage().getValue(),
                    "fingerprint", serializeFingerprint(e.getFingerprint())
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "SENT" -> SentEvent.of(
                    RequestId.of((String) payload.get("requestId")),
                    deserializeSender((Map<String, Object>) payload.get("sender")),
                    Subject.of((String) payload.get("subject")),
                    Message.of((String) payload.get("message")),
                    deserializeFingerprint((Map<String, Object>) payload.get("fingerprint"))
            );
            case "DELETED" -> DeletedEvent.of();
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

    private Map<String, Object> serializeFingerprint(Fingerprint fingerprint) {
        var result = new HashMap<String, Object>();

        fingerprint.getIpAddress().ifPresent(ipAddress -> result.put("ipAddress", ipAddress));

        return result;
    }

    private Fingerprint deserializeFingerprint(Map<String, Object> payload) {
        var ipAddress = payload.containsKey("ipAddress") ? (String) payload.get("ipAddress") : null;

        return Fingerprint.of(ipAddress);
    }

}
