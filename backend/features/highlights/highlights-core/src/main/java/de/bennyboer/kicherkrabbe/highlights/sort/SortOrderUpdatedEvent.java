package de.bennyboer.kicherkrabbe.highlights.sort;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SortOrderUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("SORT_ORDER_UPDATED");

    public static final Version VERSION = Version.zero();

    long sortOrder;

    public static SortOrderUpdatedEvent of(long sortOrder) {
        return new SortOrderUpdatedEvent(sortOrder);
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
