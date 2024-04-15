package de.bennyboer.kicherkrabbe.auth.internal.credentials.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UsageSucceededEvent implements Event {

    public static final EventName NAME = EventName.of("USAGE_SUCCEEDED");

    public static final Version VERSION = Version.zero();

    Instant date;

    public static UsageSucceededEvent of(Instant date) {
        notNull(date, "Date must be given");

        return new UsageSucceededEvent(date);
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
