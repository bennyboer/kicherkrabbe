package de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer;

import de.bennyboer.kicherkrabbe.permissions.Resource;
import de.bennyboer.kicherkrabbe.permissions.ResourceId;
import de.bennyboer.kicherkrabbe.permissions.ResourceType;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoResource;

import java.util.Optional;

public class MongoResourceSerializer {

    public static MongoResource serialize(Resource resource) {
        var result = new MongoResource();

        result.id = resource.getId()
                .map(ResourceId::getValue)
                .orElse(null);
        result.type = resource.getType().getName();

        return result;
    }

    public static Resource deserialize(MongoResource resource) {
        ResourceId resourceId = Optional.ofNullable(resource.id)
                .map(ResourceId::of)
                .orElse(null);
        ResourceType resourceType = ResourceType.of(resource.type);

        return Resource.of(resourceType, resourceId);
    }

}
