package de.bennyboer.kicherkrabbe.fabrics.rename;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.FabricName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenameCmd implements Command {

    FabricName name;

    public static RenameCmd of(FabricName name) {
        notNull(name, "Fabric name must be given");

        return new RenameCmd(name);
    }

}
