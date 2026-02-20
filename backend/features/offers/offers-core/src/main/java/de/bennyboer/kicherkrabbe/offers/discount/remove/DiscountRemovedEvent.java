package de.bennyboer.kicherkrabbe.offers.discount.remove;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DiscountRemovedEvent implements Event {

    public static final EventName NAME = EventName.of("DISCOUNT_REMOVED");

    public static final Version VERSION = Version.zero();

    public static DiscountRemovedEvent of() {
        return new DiscountRemovedEvent();
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
