package de.bennyboer.kicherkrabbe.money;

import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Money {

    long amount;

    Currency currency;

    public static Money of(long amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money euro(long amount) {
        return new Money(amount, Currency.euro());
    }

    public boolean isPositiveOrZero() {
        return amount >= 0;
    }

}
