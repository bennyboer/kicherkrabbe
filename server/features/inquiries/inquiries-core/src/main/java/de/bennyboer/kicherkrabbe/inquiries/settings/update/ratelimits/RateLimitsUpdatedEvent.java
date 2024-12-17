package de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.inquiries.settings.RateLimits;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RateLimitsUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("UPDATED_RATE_LIMITS");

    public static final Version VERSION = Version.zero();

    RateLimits rateLimits;

    public static RateLimitsUpdatedEvent of(RateLimits rateLimits) {
        notNull(rateLimits, "Rate limits must be given");

        return new RateLimitsUpdatedEvent(rateLimits);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
