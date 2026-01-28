package de.bennyboer.kicherkrabbe.fabrics;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ColorId {

    String value;

    public static ColorId of(String value) {
        notNull(value, "Color ID must be given");
        check(!value.isBlank(), "Color ID must not be blank");

        return new ColorId(value);
    }

    @Override
    public String toString() {
        return "ColorId(%s)".formatted(value);
    }

}
