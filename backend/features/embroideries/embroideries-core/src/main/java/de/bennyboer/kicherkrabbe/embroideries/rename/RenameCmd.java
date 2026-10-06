package de.bennyboer.kicherkrabbe.embroideries.rename;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenameCmd implements Command {

    EmbroideryName name;

    public static RenameCmd of(EmbroideryName name) {
        notNull(name, "Embroidery name must be given");

        return new RenameCmd(name);
    }

}
