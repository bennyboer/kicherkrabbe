package de.bennyboer.kicherkrabbe.mailing.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailing.settings.ApiToken;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunSettings;
import de.bennyboer.kicherkrabbe.mailing.settings.RateLimitSettings;
import de.bennyboer.kicherkrabbe.mailing.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear.MailgunApiTokenClearedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update.MailgunApiTokenUpdatedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.ratelimit.update.RateLimitUpdatedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.snapshot.SnapshottedEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SettingsEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case InitEvent e -> Map.of(
                    "rateLimit", serializeRateLimitSettings(e.getRateLimit()),
                    "mailgun", serializeMailgunSettings(e.getMailgun())
            );
            case SnapshottedEvent e -> Map.of(
                    "rateLimit", serializeRateLimitSettings(e.getRateLimit()),
                    "mailgun", serializeMailgunSettings(e.getMailgun())
            );
            case RateLimitUpdatedEvent e -> Map.of(
                    "duration", e.getDuration().toString(),
                    "limit", e.getLimit()
            );
            case MailgunApiTokenUpdatedEvent e -> Map.of(
                    "apiToken", e.getApiToken().getValue()
            );
            case MailgunApiTokenClearedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "INITIALIZED" -> InitEvent.of(
                    deserializeRateLimitSettings((Map<String, Object>) payload.get("rateLimit")),
                    deserializeMailgunSettings((Map<String, Object>) payload.get("mailgun"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    deserializeRateLimitSettings((Map<String, Object>) payload.get("rateLimit")),
                    deserializeMailgunSettings((Map<String, Object>) payload.get("mailgun"))
            );
            case "RATE_LIMIT_UPDATED" -> RateLimitUpdatedEvent.of(
                    Duration.parse((String) payload.get("duration")),
                    (long) payload.get("limit")
            );
            case "MAILGUN_API_TOKEN_UPDATED" -> MailgunApiTokenUpdatedEvent.of(
                    ApiToken.of((String) payload.get("apiToken"))
            );
            case "MAILGUN_API_TOKEN_CLEARED" -> MailgunApiTokenClearedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeRateLimitSettings(RateLimitSettings rateLimit) {
        return Map.of(
                "duration", rateLimit.getDuration().toString(),
                "limit", rateLimit.getLimit()
        );
    }

    private RateLimitSettings deserializeRateLimitSettings(Map<String, Object> payload) {
        return RateLimitSettings.of(
                Duration.parse((String) payload.get("duration")),
                (long) payload.get("limit")
        );
    }

    private Map<String, Object> serializeMailgunSettings(MailgunSettings mailgun) {
        var result = new HashMap<String, Object>();
        mailgun.getApiToken().ifPresent(apiToken -> result.put("apiToken", apiToken.getValue()));
        return result;
    }

    private MailgunSettings deserializeMailgunSettings(Map<String, Object> payload) {
        var apiToken = Optional.ofNullable((String) payload.get("apiToken"))
                .map(ApiToken::of)
                .orElse(null);

        return MailgunSettings.of(apiToken);
    }

}
