package de.bennyboer.kicherkrabbe.fabrics.update.availability;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeAvailability;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AvailabilityUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("AVAILABILITY_UPDATED");

    public static final Version VERSION = Version.zero();

    Set<FabricTypeAvailability> availability;

    public static AvailabilityUpdatedEvent of(Set<FabricTypeAvailability> availability) {
        notNull(availability, "Availability must be given");

        return new AvailabilityUpdatedEvent(availability);
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
