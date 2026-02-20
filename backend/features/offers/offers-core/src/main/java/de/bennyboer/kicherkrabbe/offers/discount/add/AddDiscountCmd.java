package de.bennyboer.kicherkrabbe.offers.discount.add;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.money.Money;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AddDiscountCmd implements Command {

    Money discountedPrice;

    public static AddDiscountCmd of(Money discountedPrice) {
        notNull(discountedPrice, "Discounted price must be given");

        return new AddDiscountCmd(discountedPrice);
    }

}
