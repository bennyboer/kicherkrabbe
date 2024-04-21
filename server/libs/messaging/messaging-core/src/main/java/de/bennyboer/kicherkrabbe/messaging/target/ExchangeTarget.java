package de.bennyboer.kicherkrabbe.messaging.target;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ExchangeTarget {

    String name;

    public static ExchangeTarget of(String name) {
        notNull(name, "Exchange name must be given");
        check(!name.isBlank(), "Exchange name must not be empty");

        return new ExchangeTarget(name);
    }

    public static ExchangeTarget dead() {
        return of("dead");
    }

    @Override
    public String toString() {
        return "ExchangeTarget(%s)".formatted(name);
    }

}
