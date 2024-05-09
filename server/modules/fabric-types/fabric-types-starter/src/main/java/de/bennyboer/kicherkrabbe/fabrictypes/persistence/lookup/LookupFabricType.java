package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupFabricType {

    FabricTypeId id;

    Version version;

    FabricTypeName name;

    Instant createdAt;

    public static LookupFabricType of(FabricTypeId id, Version version, FabricTypeName name, Instant createdAt) {
        notNull(id, "Fabric type ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(createdAt, "Creation date must be given");

        return new LookupFabricType(id, version, name, createdAt);
    }

}
