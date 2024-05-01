package de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer;

import de.bennyboer.kicherkrabbe.permissions.Action;
import de.bennyboer.kicherkrabbe.permissions.Permission;
import de.bennyboer.kicherkrabbe.permissions.PermissionId;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermission;

import java.time.Instant;

public class MongoPermissionSerializer {

    public static MongoPermission serialize(PermissionId permissionId, Permission permission, Instant createdAt) {
        var result = new MongoPermission();

        result.id = permissionId.getValue();
        result.resource = MongoResourceSerializer.serialize(permission.getResource());
        result.holder = MongoHolderSerializer.serialize(permission.getHolder());
        result.action = permission.getAction().getName();
        result.createdAt = createdAt;

        return result;
    }

    public static Permission deserialize(MongoPermission permission) {
        var resource = MongoResourceSerializer.deserialize(permission.resource);
        var holder = MongoHolderSerializer.deserialize(permission.holder);
        var action = Action.of(permission.action);

        return Permission.of(holder, action, resource);
    }

}
