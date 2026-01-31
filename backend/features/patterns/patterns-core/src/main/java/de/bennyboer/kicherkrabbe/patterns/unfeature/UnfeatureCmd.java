package de.bennyboer.kicherkrabbe.patterns.unfeature;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UnfeatureCmd implements Command {

    public static UnfeatureCmd of() {
        return new UnfeatureCmd();
    }

}
