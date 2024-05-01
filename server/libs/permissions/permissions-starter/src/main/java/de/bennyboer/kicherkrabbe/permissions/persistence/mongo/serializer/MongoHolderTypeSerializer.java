package de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer;

import de.bennyboer.kicherkrabbe.permissions.HolderType;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoHolderType;

public class MongoHolderTypeSerializer {

    public static MongoHolderType serialize(HolderType type) {
        return switch (type) {
            case HolderType.USER -> MongoHolderType.USER;
            case HolderType.GROUP -> MongoHolderType.GROUP;
        };
    }

    public static HolderType deserialize(MongoHolderType type) {
        return switch (type) {
            case USER -> HolderType.USER;
            case GROUP -> HolderType.GROUP;
        };
    }

}
