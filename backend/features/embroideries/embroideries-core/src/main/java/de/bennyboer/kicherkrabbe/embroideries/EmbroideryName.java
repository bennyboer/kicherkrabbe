package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryName {

    String value;

    public static EmbroideryName of(String value) {
        notNull(value, "Embroidery name must be given");
        check(!value.isBlank(), "Embroidery name must not be blank");

        return new EmbroideryName(value);
    }

    @Override
    public String toString() {
        return "EmbroideryName(%s)".formatted(value);
    }

}
