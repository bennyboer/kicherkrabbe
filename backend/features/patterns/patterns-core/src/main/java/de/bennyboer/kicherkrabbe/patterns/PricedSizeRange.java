package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.money.Money;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PricedSizeRange {

    long from;

    @Nullable
    @Getter(NONE)
    Long to;

    @Nullable
    @Getter(NONE)
    String unit;

    Money price;

    public static PricedSizeRange of(long from, @Nullable Long to, @Nullable String unit, Money price) {
        notNull(price, "Price must be given");
        check(price.isPositiveOrZero(), "Price must be positive or zero");
        check(from >= 0, "From must be positive or zero");
        if (to != null) {
            check(from <= to, "From must be less than or equal to to");
            check(to >= 0, "To must be positive or zero");
        }

        return new PricedSizeRange(from, to, unit, price);
    }

    public Optional<Long> getTo() {
        return Optional.ofNullable(to);
    }

    public Optional<String> getUnit() {
        return Optional.ofNullable(unit);
    }

    @Override
    public String toString() {
        return "PricedSizeRange(from=%s, to=%s, unit=%s, price=%s)".formatted(from, to, unit, price);
    }

}
