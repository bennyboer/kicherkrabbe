package de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.transformer;

import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessageTargetType;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTargetType;

public class MongoMessageTargetTypeTransformer {

    public static MongoMessageTargetType toMongoMessageTargetType(MessageTargetType targetType) {
        return switch (targetType) {
            case EXCHANGE -> MongoMessageTargetType.EXCHANGE;
        };
    }

    public static MessageTargetType toMessageTargetType(MongoMessageTargetType targetType) {
        return switch (targetType) {
            case EXCHANGE -> MessageTargetType.EXCHANGE;
        };
    }

}
