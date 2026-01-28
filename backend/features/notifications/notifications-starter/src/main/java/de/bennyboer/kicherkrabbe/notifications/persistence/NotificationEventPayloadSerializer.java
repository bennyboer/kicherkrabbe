package de.bennyboer.kicherkrabbe.notifications.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.Telegram;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.TelegramChatId;
import de.bennyboer.kicherkrabbe.notifications.notification.*;
import de.bennyboer.kicherkrabbe.notifications.notification.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.notifications.notification.send.SentEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NotificationEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case SentEvent e -> Map.of(
                    "origin", serializeOrigin(e.getOrigin()),
                    "target", serializeTarget(e.getTarget()),
                    "channels", serializeChannels(e.getChannels()),
                    "title", e.getTitle().getValue(),
                    "message", e.getMessage().getValue()
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "SENT" -> SentEvent.of(
                    deserializeOrigin((Map<String, Object>) payload.get("origin")),
                    deserializeTarget((Map<String, Object>) payload.get("target")),
                    deserializeChannels((List<Map<String, Object>>) payload.get("channels")),
                    Title.of((String) payload.get("title")),
                    Message.of((String) payload.get("message"))
            );
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private List<Map<String, Object>> serializeChannels(Set<Channel> channels) {
        return channels.stream()
                .map(this::serializeChannel)
                .toList();
    }

    private Set<Channel> deserializeChannels(List<Map<String, Object>> payload) {
        return payload.stream()
                .map(this::deserializeChannel)
                .collect(Collectors.toSet());
    }

    private Map<String, Object> serializeChannel(Channel channel) {
        return switch (channel.getType()) {
            case EMAIL -> Map.of(
                    "type", serializeChannelType(channel.getType()),
                    "mail", channel.getMail().orElseThrow().getValue()
            );
            case TELEGRAM -> Map.of(
                    "type", serializeChannelType(channel.getType()),
                    "telegram", serializeTelegram(channel.getTelegram().orElseThrow())
            );
            case UNKNOWN -> throw new IllegalStateException("Unexpected channel type: " + channel.getType());
        };
    }

    private Channel deserializeChannel(Map<String, Object> payload) {
        var type = deserializeChannelType((String) payload.get("type"));

        return switch (type) {
            case EMAIL -> Channel.mail(EMail.of((String) payload.get("mail")));
            case TELEGRAM -> Channel.telegram(deserializeTelegram((Map<String, Object>) payload.get("telegram")));
            case UNKNOWN -> throw new IllegalStateException("Unexpected channel type: " + type);
        };
    }

    private String serializeChannelType(ChannelType type) {
        return switch (type) {
            case EMAIL -> "EMAIL";
            case TELEGRAM -> "TELEGRAM";
            case UNKNOWN -> throw new IllegalStateException("Unexpected channel type: " + type);
        };
    }

    private ChannelType deserializeChannelType(String type) {
        return switch (type) {
            case "EMAIL" -> ChannelType.EMAIL;
            case "TELEGRAM" -> ChannelType.TELEGRAM;
            default -> throw new IllegalStateException("Unexpected channel type: " + type);
        };
    }

    private Map<String, Object> serializeTelegram(Telegram telegram) {
        return Map.of(
                "chatId", telegram.getChatId().getValue()
        );
    }

    private Telegram deserializeTelegram(Map<String, Object> payload) {
        return Telegram.of(TelegramChatId.of((String) payload.get("chatId")));
    }

    private Map<String, Object> serializeTarget(Target target) {
        return Map.of(
                "type", serializeTargetType(target.getType()),
                "id", target.getId().getValue()
        );
    }

    private Target deserializeTarget(Map<String, Object> payload) {
        var type = deserializeTargetType((String) payload.get("type"));
        var id = TargetId.of((String) payload.get("id"));

        return Target.of(type, id);
    }

    private String serializeTargetType(TargetType type) {
        return switch (type) {
            case SYSTEM -> "SYSTEM";
        };
    }

    private TargetType deserializeTargetType(String type) {
        return switch (type) {
            case "SYSTEM" -> TargetType.SYSTEM;
            default -> throw new IllegalStateException("Unexpected target type: " + type);
        };
    }

    private Map<String, Object> serializeOrigin(Origin origin) {
        return Map.of(
                "type", serializeOriginType(origin.getType()),
                "id", origin.getId().getValue()
        );
    }

    private Origin deserializeOrigin(Map<String, Object> payload) {
        var type = deserializeOriginType((String) payload.get("type"));
        var id = OriginId.of((String) payload.get("id"));

        return Origin.of(type, id);
    }

    private String serializeOriginType(OriginType type) {
        return switch (type) {
            case MAIL -> "MAIL";
        };
    }

    private OriginType deserializeOriginType(String type) {
        return switch (type) {
            case "MAIL" -> OriginType.MAIL;
            default -> throw new IllegalStateException("Unexpected origin type: " + type);
        };
    }

}
