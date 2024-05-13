package de.bennyboer.kicherkrabbe.fabrics.delete.fabrictype;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveFabricTypeCmd implements Command {

    FabricTypeId fabricTypeId;

    public static RemoveFabricTypeCmd of(FabricTypeId fabricTypeId) {
        notNull(fabricTypeId, "Fabric type ID to remove must be given");

        return new RemoveFabricTypeCmd(fabricTypeId);
    }

}

