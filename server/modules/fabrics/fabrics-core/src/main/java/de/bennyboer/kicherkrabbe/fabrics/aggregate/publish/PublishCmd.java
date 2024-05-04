package de.bennyboer.kicherkrabbe.fabrics.aggregate.publish;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PublishCmd implements Command {

    public static PublishCmd of() {
        return new PublishCmd();
    }

}
