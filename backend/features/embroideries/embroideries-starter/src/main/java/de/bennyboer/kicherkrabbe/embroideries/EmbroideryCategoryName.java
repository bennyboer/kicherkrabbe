package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryCategoryName {

    String value;

    public static EmbroideryCategoryName of(String value) {
        notNull(value, "Embroidery category name must be given");
        check(!value.isBlank(), "Embroidery category name must not be blank");

        return new EmbroideryCategoryName(value);
    }

    @Override
    public String toString() {
        return "EmbroideryCategoryName(%s)".formatted(value);
    }

}
