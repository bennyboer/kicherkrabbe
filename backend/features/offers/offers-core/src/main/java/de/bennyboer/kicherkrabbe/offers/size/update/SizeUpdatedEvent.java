package de.bennyboer.kicherkrabbe.offers.size.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.offers.OfferSize;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SizeUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("SIZE_UPDATED");

    public static final Version VERSION = Version.zero();

    OfferSize size;

    public static SizeUpdatedEvent of(OfferSize size) {
        notNull(size, "Size must be given");

        return new SizeUpdatedEvent(size);
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
