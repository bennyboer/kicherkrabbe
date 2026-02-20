package de.bennyboer.kicherkrabbe.offers.unreserve;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UnreservedEvent implements Event {

    public static final EventName NAME = EventName.of("UNRESERVED");

    public static final Version VERSION = Version.zero();

    public static UnreservedEvent of() {
        return new UnreservedEvent();
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
