package de.bennyboer.kicherkrabbe.fabrics;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricKind {

    public static final FabricKind PATTERNED = new FabricKind("PATTERNED");

    public static final FabricKind SOLID_COLOR = new FabricKind("SOLID_COLOR");

    String value;

    public static FabricKind of(String value) {
        notNull(value, "Fabric kind must be given");
        check(!value.isBlank(), "Fabric kind must not be blank");

        return new FabricKind(value);
    }

}
