package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryCategoryId {

    String value;

    public static EmbroideryCategoryId of(String value) {
        notNull(value, "Embroidery category ID must be given");
        check(!value.isBlank(), "Embroidery category ID must not be blank");

        return new EmbroideryCategoryId(value);
    }

    @Override
    public String toString() {
        return "EmbroideryCategoryId(%s)".formatted(value);
    }

}
