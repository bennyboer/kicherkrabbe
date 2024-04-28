package de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer;

import de.bennyboer.kicherkrabbe.permissions.Holder;
import de.bennyboer.kicherkrabbe.permissions.HolderId;
import de.bennyboer.kicherkrabbe.permissions.HolderType;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoHolder;

public class MongoHolderSerializer {

    public static MongoHolder serialize(Holder holder) {
        var result = new MongoHolder();

        result.id = holder.getId().getValue();
        result.type = MongoHolderTypeSerializer.serialize(holder.getType());

        return result;
    }

    public static Holder deserialize(MongoHolder holder) {
        HolderType type = MongoHolderTypeSerializer.deserialize(holder.type);
        var id = HolderId.of(holder.id);

        return switch (type) {
            case USER -> Holder.user(id);
            case GROUP -> Holder.group(id);
        };
    }

}
