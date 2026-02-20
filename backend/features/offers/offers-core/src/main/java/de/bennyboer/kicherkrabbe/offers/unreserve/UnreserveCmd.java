package de.bennyboer.kicherkrabbe.offers.unreserve;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UnreserveCmd implements Command {

    public static UnreserveCmd of() {
        return new UnreserveCmd();
    }

}
