package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricType {

    FabricTypeId id;

    FabricTypeName name;

    public static FabricType of(FabricTypeId id, FabricTypeName name) {
        notNull(id, "Fabric type ID must be given");
        notNull(name, "Fabric type name must be given");

        return new FabricType(id, name);
    }

}
