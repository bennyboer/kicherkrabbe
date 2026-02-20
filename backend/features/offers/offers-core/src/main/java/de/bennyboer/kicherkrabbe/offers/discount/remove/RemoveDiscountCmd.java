package de.bennyboer.kicherkrabbe.offers.discount.remove;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveDiscountCmd implements Command {

    public static RemoveDiscountCmd of() {
        return new RemoveDiscountCmd();
    }

}
