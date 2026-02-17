package de.bennyboer.kicherkrabbe.assets.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.LookupAsset;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;

public class MongoAssetLookupSerializer implements ReadModelSerializer<LookupAsset, MongoLookupAsset> {

    @Override
    public MongoLookupAsset serialize(LookupAsset readModel) {
        var result = new MongoLookupAsset();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.contentType = readModel.getContentType().getValue();
        result.fileSize = readModel.getFileSize();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupAsset deserialize(MongoLookupAsset serialized) {
        return LookupAsset.of(
                AssetId.of(serialized.id),
                Version.of(serialized.version),
                ContentType.of(serialized.contentType),
                serialized.fileSize,
                serialized.createdAt
        );
    }

}
