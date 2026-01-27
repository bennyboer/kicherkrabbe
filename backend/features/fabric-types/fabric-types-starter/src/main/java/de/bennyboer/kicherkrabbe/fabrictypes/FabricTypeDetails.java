package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FabricTypeDetails {

    FabricTypeId id;

    Version version;

    FabricTypeName name;

    Instant createdAt;

    public static FabricTypeDetails of(FabricTypeId id, Version version, FabricTypeName name, Instant createdAt) {
        notNull(id, "Fabric type ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(createdAt, "Creation date must be given");

        return new FabricTypeDetails(id, version, name, createdAt);
    }

}
