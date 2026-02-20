package de.bennyboer.kicherkrabbe.offers.price.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.money.Money;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdatePriceCmd implements Command {

    Money price;

    public static UpdatePriceCmd of(Money price) {
        notNull(price, "Price must be given");

        return new UpdatePriceCmd(price);
    }

}
