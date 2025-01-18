package de.bennyboer.kicherkrabbe.products.counter.increment;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class IncrementedEvent implements Event {

    public static final EventName NAME = EventName.of("INCREMENTED");

    public static final Version VERSION = Version.zero();

    public static IncrementedEvent of() {
        return new IncrementedEvent();
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
