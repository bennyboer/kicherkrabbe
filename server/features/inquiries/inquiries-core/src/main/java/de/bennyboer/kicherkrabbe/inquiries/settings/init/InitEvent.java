package de.bennyboer.kicherkrabbe.inquiries.settings.init;

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
public class InitEvent implements Event {

    public static final EventName NAME = EventName.of("INITIALIZED");

    public static final Version VERSION = Version.zero();

    boolean enabled;

    RateLimits rateLimits;

    public static InitEvent of(boolean enabled, RateLimits rateLimits) {
        notNull(rateLimits, "Rate limits must be given");

        return new InitEvent(enabled, rateLimits);
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
