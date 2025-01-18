package de.bennyboer.kicherkrabbe.products.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

/**
 * Represents a float with low precision.
 * It offers two decimal places.
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class LowPrecisionFloat {

    long value;

    public static LowPrecisionFloat of(long value) {
        check(value >= 0, "Value must be positive");

        return new LowPrecisionFloat(value);
    }

    public static LowPrecisionFloat zero() {
        return new LowPrecisionFloat(0);
    }

    public LowPrecisionFloat add(LowPrecisionFloat other) {
        return new LowPrecisionFloat(value + other.value);
    }

    @Override
    public String toString() {
        return "LowPrecisionFloat(%d)".formatted(value);
    }

}
