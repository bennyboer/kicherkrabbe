package de.bennyboer.kicherkrabbe.fabrics.colors.delete;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DeleteCmd implements Command {

    public static DeleteCmd of() {
        return new DeleteCmd();
    }

}
