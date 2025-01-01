package de.bennyboer.kicherkrabbe.mailing.settings.ratelimit.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RateLimitUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("RATE_LIMIT_UPDATED");

    public static final Version VERSION = Version.zero();

    Duration duration;

    long limit;

    public static RateLimitUpdatedEvent of(Duration duration, long limit) {
        notNull(duration, "Duration must be given");
        check(duration.isPositive(), "Duration must be positive");
        check(!duration.isZero(), "Duration must not be zero");
        check(limit >= 0, "Limit must be greater or equal to 0");

        return new RateLimitUpdatedEvent(duration, limit);
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
