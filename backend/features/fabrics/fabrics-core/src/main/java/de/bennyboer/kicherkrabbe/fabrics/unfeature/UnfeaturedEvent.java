package de.bennyboer.kicherkrabbe.fabrics.unfeature;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UnfeaturedEvent implements Event {

    public static final EventName NAME = EventName.of("UNFEATURED");

    public static final Version VERSION = Version.zero();

    public static UnfeaturedEvent of() {
        return new UnfeaturedEvent();
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
