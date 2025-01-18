package de.bennyboer.kicherkrabbe.products.product.produced.update;

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
public class ProducedAtUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("PRODUCED_AT_UPDATED");

    public static final Version VERSION = Version.zero();

    Instant producedAt;

    public static ProducedAtUpdatedEvent of(Instant producedAt) {
        notNull(producedAt, "Produced at date must be given");

        return new ProducedAtUpdatedEvent(producedAt);
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
