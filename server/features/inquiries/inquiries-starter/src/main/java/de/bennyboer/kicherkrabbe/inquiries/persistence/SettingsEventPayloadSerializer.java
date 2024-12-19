package de.bennyboer.kicherkrabbe.inquiries.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.inquiries.settings.RateLimit;
import de.bennyboer.kicherkrabbe.inquiries.settings.RateLimits;
import de.bennyboer.kicherkrabbe.inquiries.settings.disable.DisabledEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.enable.EnabledEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits.RateLimitsUpdatedEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SettingsEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case InitEvent e -> Map.of(
                    "enabled", e.isEnabled(),
                    "rateLimits", serializeRateLimits(e.getRateLimits())
            );
            case SnapshottedEvent e -> Map.of(
                    "enabled", e.isEnabled(),
                    "rateLimits", serializeRateLimits(e.getRateLimits())
            );
            case EnabledEvent ignored -> Map.of();
            case DisabledEvent ignored -> Map.of();
            case RateLimitsUpdatedEvent e -> Map.of(
                    "rateLimits", serializeRateLimits(e.getRateLimits())
            );
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "INITIALIZED" -> InitEvent.of(
                    (boolean) payload.get("enabled"),
                    deserializeRateLimits((Map<String, Object>) payload.get("rateLimits"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    (boolean) payload.get("enabled"),
                    deserializeRateLimits((Map<String, Object>) payload.get("rateLimits"))
            );
            case "ENABLED" -> EnabledEvent.of();
            case "DISABLED" -> DisabledEvent.of();
            case "UPDATED_RATE_LIMITS" -> RateLimitsUpdatedEvent.of(
                    deserializeRateLimits((Map<String, Object>) payload.get("rateLimits"))
            );
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeRateLimits(RateLimits rateLimits) {
        var result = new HashMap<String, Object>();

        result.put("perMail", serializeRateLimit(rateLimits.getPerMail()));
        result.put("perIp", serializeRateLimit(rateLimits.getPerIp()));
        result.put("overall", serializeRateLimit(rateLimits.getOverall()));

        return result;
    }

    private RateLimits deserializeRateLimits(Map<String, Object> payload) {
        var perMail = deserializeRateLimit((Map<String, Object>) payload.get("perMail"));
        var perIp = deserializeRateLimit((Map<String, Object>) payload.get("perIp"));
        var overall = deserializeRateLimit((Map<String, Object>) payload.get("overall"));

        return RateLimits.of(perMail, perIp, overall);
    }

    private Map<String, Object> serializeRateLimit(RateLimit rateLimit) {
        var result = new HashMap<String, Object>();

        result.put("maxRequests", rateLimit.getMaxRequests());
        result.put("duration", rateLimit.getDuration().toString());

        return result;
    }

    private RateLimit deserializeRateLimit(Map<String, Object> payload) {
        var maxRequests = (long) payload.get("maxRequests");
        var duration = Duration.parse((String) payload.get("duration"));

        return RateLimit.of(maxRequests, duration);
    }

}
