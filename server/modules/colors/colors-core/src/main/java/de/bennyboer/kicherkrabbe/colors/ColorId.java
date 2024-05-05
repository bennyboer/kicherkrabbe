package de.bennyboer.kicherkrabbe.colors;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
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

    public static ColorId create() {
        return new ColorId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "ColorId(%s)".formatted(value);
    }

}
