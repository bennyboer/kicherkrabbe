package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.LookupFabricType;

public class MongoLookupFabricTypeSerializer implements ReadModelSerializer<LookupFabricType, MongoLookupFabricType> {

    @Override
    public MongoLookupFabricType serialize(LookupFabricType readModel) {
        var result = new MongoLookupFabricType();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.name = readModel.getName().getValue();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupFabricType deserialize(MongoLookupFabricType serialized) {
        var id = FabricTypeId.of(serialized.id);
        var version = Version.of(serialized.version);
        var name = FabricTypeName.of(serialized.name);
        var createdAt = serialized.createdAt;

        return LookupFabricType.of(
                id,
                version,
                name,
                createdAt
        );
    }

}
