package de.bennyboer.kicherkrabbe.patterns.feature;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FeaturedEvent implements Event {

    public static final EventName NAME = EventName.of("FEATURED");

    public static final Version VERSION = Version.zero();

    public static FeaturedEvent of() {
        return new FeaturedEvent();
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
