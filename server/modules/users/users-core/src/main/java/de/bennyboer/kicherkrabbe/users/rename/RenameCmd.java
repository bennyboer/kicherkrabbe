package de.bennyboer.kicherkrabbe.users.rename;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.users.FullName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenameCmd implements Command {

    FullName name;

    public static RenameCmd of(FullName name) {
        notNull(name, "Name must be given");

        return new RenameCmd(name);
    }

}
