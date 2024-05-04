package de.bennyboer.kicherkrabbe.fabrics.aggregate.update.availability;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.aggregate.FabricTypeAvailability;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateAvailabilityCmd implements Command {

    Set<FabricTypeAvailability> availability;

    public static UpdateAvailabilityCmd of(Set<FabricTypeAvailability> availability) {
        notNull(availability, "Availability must be given");

        return new UpdateAvailabilityCmd(availability);
    }

}
