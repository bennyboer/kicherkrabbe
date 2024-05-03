package de.bennyboer.kicherkrabbe.fabrics;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricId {

    String value;

    public static FabricId of(String value) {
        notNull(value, "Fabric ID must be given");
        check(!value.isBlank(), "Fabric ID must not be blank");

        return new FabricId(value);
    }

    @Override
    public String toString() {
        return "FabricId(%s)".formatted(value);
    }

}
