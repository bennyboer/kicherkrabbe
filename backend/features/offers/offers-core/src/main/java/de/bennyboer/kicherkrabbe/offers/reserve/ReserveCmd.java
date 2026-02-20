package de.bennyboer.kicherkrabbe.offers.reserve;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ReserveCmd implements Command {

    public static ReserveCmd of() {
        return new ReserveCmd();
    }

}
