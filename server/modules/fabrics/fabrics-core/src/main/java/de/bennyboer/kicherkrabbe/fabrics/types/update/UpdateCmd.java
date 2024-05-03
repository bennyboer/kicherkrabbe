package de.bennyboer.kicherkrabbe.fabrics.types.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.types.FabricTypeName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateCmd implements Command {

    FabricTypeName name;

    public static UpdateCmd of(FabricTypeName name) {
        notNull(name, "Fabric type name must be given");

        return new UpdateCmd(name);
    }

}
