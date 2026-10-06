package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryId {

    String value;

    public static EmbroideryId of(String value) {
        notNull(value, "Embroidery ID must be given");
        check(!value.isBlank(), "Embroidery ID must not be blank");

        return new EmbroideryId(value);
    }

    public static EmbroideryId create() {
        return new EmbroideryId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "EmbroideryId(%s)".formatted(value);
    }

}
