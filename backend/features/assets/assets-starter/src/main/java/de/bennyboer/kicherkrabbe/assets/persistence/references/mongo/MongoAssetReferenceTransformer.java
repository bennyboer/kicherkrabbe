package de.bennyboer.kicherkrabbe.assets.persistence.references.mongo;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReference;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetResourceId;

public class MongoAssetReferenceTransformer {

    public static MongoAssetReference toMongo(AssetReference reference) {
        var result = new MongoAssetReference();

        result.id = reference.getAssetId().getValue()
                + "_" + reference.getResourceType().name()
                + "_" + reference.getResourceId().getValue();
        result.assetId = reference.getAssetId().getValue();
        result.resourceType = reference.getResourceType().name();
        result.resourceId = reference.getResourceId().getValue();

        return result;
    }

    public static AssetReference fromMongo(MongoAssetReference mongo) {
        return AssetReference.of(
                AssetId.of(mongo.assetId),
                AssetReferenceResourceType.valueOf(mongo.resourceType),
                AssetResourceId.of(mongo.resourceId)
        );
    }

}
