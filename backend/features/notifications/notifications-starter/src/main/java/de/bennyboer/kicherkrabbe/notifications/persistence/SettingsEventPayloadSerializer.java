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
import de.bennyboer.kicherkrabbe.notifications.settings.ActivatableChannel;
import de.bennyboer.kicherkrabbe.notifications.settings.SystemSettings;
import de.bennyboer.kicherkrabbe.notifications.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.activate.SystemChannelActivatedEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.deactivate.SystemChannelDeactivatedEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.update.SystemChannelUpdatedEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.disable.SystemNotificationsDisabledEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.enable.SystemNotificationsEnabledEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SettingsEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case InitEvent e -> Map.of(
                    "systemSettings", serializeSystemSettings(e.getSystemSettings())
            );
            case SystemNotificationsEnabledEvent ignored -> Map.of();
            case SystemNotificationsDisabledEvent ignored -> Map.of();
            case SystemChannelUpdatedEvent e -> Map.of(
                    "channel", serializeChannel(e.getChannel())
            );
            case SystemChannelActivatedEvent e -> Map.of(
                    "channelType", serializeChannelType(e.getChannelType())
            );
            case SystemChannelDeactivatedEvent e -> Map.of(
                    "channelType", serializeChannelType(e.getChannelType())
            );
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "INITIALIZED" -> InitEvent.of(deserializeSystemSettings(
                    (Map<String, Object>) payload.get("systemSettings")
            ));
            case "SYSTEM_NOTIFICATIONS_ENABLED" -> SystemNotificationsEnabledEvent.of();
            case "SYSTEM_NOTIFICATIONS_DISABLED" -> SystemNotificationsDisabledEvent.of();
            case "SYSTEM_CHANNEL_UPDATED" -> SystemChannelUpdatedEvent.of(deserializeChannel(
                    (Map<String, Object>) payload.get("channel")
            ));
            case "SYSTEM_CHANNEL_ACTIVATED" -> SystemChannelActivatedEvent.of(deserializeChannelType(
                    (String) payload.get("channelType")
            ));
            case "SYSTEM_CHANNEL_DEACTIVATED" -> SystemChannelDeactivatedEvent.of(deserializeChannelType(
                    (String) payload.get("channelType")
            ));
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeSystemSettings(SystemSettings systemSettings) {
        return Map.of(
                "enabled", systemSettings.isEnabled(),
                "channels", serializeActivatableChannels(systemSettings.getChannels())
        );
    }

    private SystemSettings deserializeSystemSettings(Map<String, Object> payload) {
        return SystemSettings.of(
                (boolean) payload.get("enabled"),
                deserializeActivatableChannels((List<Map<String, Object>>) payload.get("channels"))
        );
    }

    private List<Map<String, Object>> serializeActivatableChannels(Set<ActivatableChannel> channels) {
        return channels.stream()
                .map(this::serializeActivatableChannel)
                .toList();
    }

    private Set<ActivatableChannel> deserializeActivatableChannels(List<Map<String, Object>> payload) {
        return payload.stream()
                .map(this::deserializeActivatableChannel)
                .collect(Collectors.toSet());
    }

    private Map<String, Object> serializeActivatableChannel(ActivatableChannel channel) {
        return Map.of(
                "channel", serializeChannel(channel.getChannel()),
                "active", channel.isActive()
        );
    }

    private ActivatableChannel deserializeActivatableChannel(Map<String, Object> payload) {
        return ActivatableChannel.of(
                deserializeChannel((Map<String, Object>) payload.get("channel")),
                (boolean) payload.get("active")
        );
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

}
