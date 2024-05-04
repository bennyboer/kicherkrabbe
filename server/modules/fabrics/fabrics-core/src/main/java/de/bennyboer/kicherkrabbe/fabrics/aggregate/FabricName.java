package de.bennyboer.kicherkrabbe.fabrics.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricName {

    String value;

    public static FabricName of(String value) {
        notNull(value, "Fabric name must be given");
        check(!value.isBlank(), "Fabric name must not be blank");

        return new FabricName(value);
    }

    @Override
    public String toString() {
        return "FabricName(%s)".formatted(value);
    }

}
