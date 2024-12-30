package de.bennyboer.kicherkrabbe.telegram.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import de.bennyboer.kicherkrabbe.telegram.settings.BotSettings;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.clear.BotApiTokenClearedEvent;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.update.BotApiTokenUpdatedEvent;
import de.bennyboer.kicherkrabbe.telegram.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.telegram.settings.snapshot.SnapshottedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SettingsEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case InitEvent e -> Map.of(
                    "botSettings", serializeBotSettings(e.getBotSettings())
            );
            case SnapshottedEvent e -> Map.of(
                    "botSettings", serializeBotSettings(e.getBotSettings())
            );
            case BotApiTokenUpdatedEvent e -> Map.of(
                    "apiToken", e.getApiToken().getValue()
            );
            case BotApiTokenClearedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "INITIALIZED" -> InitEvent.of(deserializeBotSettings(
                    (Map<String, Object>) payload.get("botSettings")
            ));
            case "SNAPSHOTTED" -> SnapshottedEvent.of(deserializeBotSettings(
                    (Map<String, Object>) payload.get("botSettings")
            ));
            case "BOT_API_TOKEN_UPDATED" -> BotApiTokenUpdatedEvent.of(ApiToken.of((String) payload.get("apiToken")));
            case "BOT_API_TOKEN_CLEARED" -> BotApiTokenClearedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeBotSettings(BotSettings botSettings) {
        var result = new HashMap<String, Object>();
        botSettings.getApiToken().ifPresent(apiToken -> result.put("apiToken", apiToken.getValue()));
        return result;
    }

    private BotSettings deserializeBotSettings(Map<String, Object> payload) {
        var apiToken = Optional.ofNullable((String) payload.get("apiToken"))
                .map(ApiToken::of)
                .orElse(null);

        return BotSettings.of(apiToken);
    }

}
