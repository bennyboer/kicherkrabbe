package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypeName {

    String value;

    public static FabricTypeName of(String value) {
        notNull(value, "Fabric type name must be given");
        check(!value.isBlank(), "Fabric type name must not be blank");

        return new FabricTypeName(value);
    }

    @Override
    public String toString() {
        return "FabricTypeName(%s)".formatted(value);
    }

}
