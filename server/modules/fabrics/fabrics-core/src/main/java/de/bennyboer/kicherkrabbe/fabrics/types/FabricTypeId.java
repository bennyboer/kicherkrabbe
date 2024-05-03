package de.bennyboer.kicherkrabbe.fabrics.types;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypeId {

    String value;

    public static FabricTypeId of(String value) {
        notNull(value, "Fabric type ID must be given");
        check(!value.isBlank(), "Fabric type ID must not be blank");

        return new FabricTypeId(value);
    }

    public static FabricTypeId create() {
        return new FabricTypeId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "FabricTypeId(%s)".formatted(value);
    }

}
