package de.bennyboer.kicherkrabbe.patterns.rename;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.patterns.PatternName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RenameCmd implements Command {

    PatternName name;

    public static RenameCmd of(PatternName name) {
        notNull(name, "Pattern name must be given");

        return new RenameCmd(name);
    }

}
