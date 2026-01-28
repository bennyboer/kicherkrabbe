package de.bennyboer.kicherkrabbe.colors;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ColorName {

    String value;

    public static ColorName of(String value) {
        notNull(value, "Color name must be given");
        check(!value.isBlank(), "Color name must not be blank");

        return new ColorName(value);
    }

    @Override
    public String toString() {
        return "ColorName(%s)".formatted(value);
    }

}
