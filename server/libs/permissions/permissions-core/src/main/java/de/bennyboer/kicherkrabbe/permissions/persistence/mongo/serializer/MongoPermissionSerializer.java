package de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer;

import de.bennyboer.kicherkrabbe.permissions.Action;
import de.bennyboer.kicherkrabbe.permissions.Permission;
import de.bennyboer.kicherkrabbe.permissions.PermissionId;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermission;

public class MongoPermissionSerializer {

    public static MongoPermission serialize(Permission permission) {
        var result = new MongoPermission();

        result.id = permission.getId().getValue();
        result.resource = MongoResourceSerializer.serialize(permission.getResource());
        result.holder = MongoHolderSerializer.serialize(permission.getHolder());
        result.action = permission.getAction().getName();
        result.createdAt = permission.getCreatedAt();

        return result;
    }

    public static Permission deserialize(MongoPermission permission) {
        var id = PermissionId.of(permission.id);
        var resource = MongoResourceSerializer.deserialize(permission.resource);
        var holder = MongoHolderSerializer.deserialize(permission.holder);
        var action = Action.of(permission.action);
        var createdAt = permission.createdAt;

        return Permission.of(id, holder, action, resource, createdAt);
    }

}
