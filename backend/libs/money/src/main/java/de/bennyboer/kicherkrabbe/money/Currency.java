package de.bennyboer.kicherkrabbe.money;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Currency {

    String name;

    String shortForm;

    String symbol;

    private static Currency of(String name, String shortForm, String symbol) {
        notNull(name, "Currency name must be given");
        notNull(shortForm, "Currency short form must be given");
        notNull(symbol, "Currency symbol must be given");
        check(!name.isBlank(), "Currency name must not be blank");
        check(!shortForm.isBlank(), "Currency short form must not be blank");
        check(!symbol.isBlank(), "Currency symbol must not be blank");

        return new Currency(name, shortForm, symbol);
    }

    public static Currency euro() {
        return of("Euro", "EUR", "€");
    }

    public static Currency fromShortForm(String shortForm) {
        return switch (shortForm) {
            case "EUR" -> euro();
            default -> throw new IllegalArgumentException("Unknown currency short form: " + shortForm);
        };
    }

}
