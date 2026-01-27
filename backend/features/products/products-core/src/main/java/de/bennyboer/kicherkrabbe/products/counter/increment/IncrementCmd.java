package de.bennyboer.kicherkrabbe.products.counter.increment;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class IncrementCmd implements Command {

    public static IncrementCmd of() {
        return new IncrementCmd();
    }

}
