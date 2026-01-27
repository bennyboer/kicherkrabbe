package de.bennyboer.kicherkrabbe.categories.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.LookupCategory;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;

public class MongoLookupCategorySerializer implements ReadModelSerializer<LookupCategory, MongoLookupCategory> {

    @Override
    public MongoLookupCategory serialize(LookupCategory readModel) {
        var result = new MongoLookupCategory();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.name = readModel.getName().getValue();
        result.group = switch (readModel.getGroup()) {
            case CLOTHING -> "CLOTHING";
            case NONE -> "NONE";
        };
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupCategory deserialize(MongoLookupCategory serialized) {
        var id = CategoryId.of(serialized.id);
        var version = Version.of(serialized.version);
        var name = CategoryName.of(serialized.name);
        var group = switch (serialized.group) {
            case "CLOTHING" -> CategoryGroup.CLOTHING;
            case "NONE" -> CategoryGroup.NONE;
            default -> throw new IllegalArgumentException("Unknown category group: " + serialized.group);
        };
        var createdAt = serialized.createdAt;

        return LookupCategory.of(
                id,
                version,
                name,
                group,
                createdAt
        );
    }

}
