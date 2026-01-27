package de.bennyboer.kicherkrabbe.fabrictypes.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    FabricTypeName name;

    public static CreateCmd of(FabricTypeName name) {
        notNull(name, "Fabric type name must be given");

        return new CreateCmd(name);
    }

}
