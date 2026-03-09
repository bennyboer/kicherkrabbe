package de.bennyboer.kicherkrabbe.fabrics;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public enum FabricKind {

    PATTERNED,
    SOLID_COLOR;

    public String getValue() {
        return name();
    }

    public static FabricKind of(String value) {
        notNull(value, "Fabric kind must be given");
        return valueOf(value);
    }

}
