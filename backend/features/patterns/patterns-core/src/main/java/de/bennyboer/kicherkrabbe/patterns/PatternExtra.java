package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.money.Money;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternExtra {

    PatternExtraName name;

    Money price;

    public static PatternExtra of(PatternExtraName name, Money price) {
        notNull(name, "Pattern extra name must be given");
        notNull(price, "Price must be given");
        check(price.isPositiveOrZero(), "Price must be positive or zero");

        return new PatternExtra(name, price);
    }

}
