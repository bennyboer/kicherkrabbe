package de.bennyboer.kicherkrabbe.patterns.unpublish;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UnpublishCmd implements Command {

    public static UnpublishCmd of() {
        return new UnpublishCmd();
    }

}
