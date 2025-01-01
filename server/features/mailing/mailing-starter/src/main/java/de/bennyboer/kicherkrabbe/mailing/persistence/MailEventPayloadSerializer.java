package de.bennyboer.kicherkrabbe.mailing.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailing.MailingService;
import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import de.bennyboer.kicherkrabbe.mailing.mail.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.mailing.mail.send.SentEvent;
import de.bennyboer.kicherkrabbe.mailing.mail.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MailEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case SentEvent e -> Map.of(
                    "sender", serializeSender(e.getSender()),
                    "receivers", serializeReceivers(e.getReceivers()),
                    "subject", e.getSubject().getValue(),
                    "text", e.getText().getValue(),
                    "mailingService", serializeMailingService(e.getMailingService())
            );
            case SnapshottedEvent e -> {
                var result = new HashMap<String, Object>();

                result.put("sender", serializeSender(e.getSender()));
                result.put("receivers", serializeReceivers(e.getReceivers()));
                result.put("subject", e.getSubject().getValue());
                result.put("text", e.getText().getValue());
                result.put("mailingService", serializeMailingService(e.getMailingService()));
                result.put("sentAt", e.getSentAt().toString());
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
            case "SENT" -> SentEvent.of(
                    deserializeSender((Map<String, Object>) payload.get("sender")),
                    deserializeReceivers((List<Map<String, Object>>) payload.get("receivers")),
                    Subject.of((String) payload.get("subject")),
                    Text.of((String) payload.get("text")),
                    deserializeMailingService((String) payload.get("mailingService"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    deserializeSender((Map<String, Object>) payload.get("sender")),
                    deserializeReceivers((List<Map<String, Object>>) payload.get("receivers")),
                    Subject.of((String) payload.get("subject")),
                    Text.of((String) payload.get("text")),
                    deserializeMailingService((String) payload.get("mailingService")),
                    Instant.parse((String) payload.get("sentAt")),
                    payload.containsKey("deletedAt") ? Instant.parse((String) payload.get("deletedAt")) : null
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private String serializeMailingService(MailingService mailingService) {
        return switch (mailingService) {
            case MAILGUN -> "MAILGUN";
        };
    }

    private MailingService deserializeMailingService(String value) {
        return switch (value) {
            case "MAILGUN" -> MailingService.MAILGUN;
            default -> throw new IllegalStateException("Unexpected mailing service: " + value);
        };
    }

    private Map<String, Object> serializeSender(Sender sender) {
        return Map.of(
                "mail", sender.getMail().getValue()
        );
    }

    private Sender deserializeSender(Map<String, Object> payload) {
        return Sender.of(
                EMail.of((String) payload.get("mail"))
        );
    }

    private List<Map<String, Object>> serializeReceivers(Set<Receiver> receivers) {
        return receivers.stream()
                .map(this::serializeReceiver)
                .toList();
    }

    private Set<Receiver> deserializeReceivers(List<Map<String, Object>> payload) {
        return payload.stream()
                .map(this::deserializeReceiver)
                .collect(Collectors.toSet());
    }

    private Map<String, Object> serializeReceiver(Receiver receiver) {
        return Map.of(
                "mail", receiver.getMail().getValue()
        );
    }

    private Receiver deserializeReceiver(Map<String, Object> payload) {
        return Receiver.of(
                EMail.of((String) payload.get("mail"))
        );
    }

}
