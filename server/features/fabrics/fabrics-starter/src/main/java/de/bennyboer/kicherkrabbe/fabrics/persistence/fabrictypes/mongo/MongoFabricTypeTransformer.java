package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.mongo;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeName;

public class MongoFabricTypeTransformer {

    public static MongoFabricType toMongo(FabricType fabricType) {
        var result = new MongoFabricType();

        result.id = fabricType.getId().getValue();
        result.name = fabricType.getName().getValue();

        return result;
    }

    public static FabricType fromMongo(MongoFabricType fabricType) {
        var id = FabricTypeId.of(fabricType.id);
        var name = FabricTypeName.of(fabricType.name);

        return FabricType.of(id, name);
    }

}
