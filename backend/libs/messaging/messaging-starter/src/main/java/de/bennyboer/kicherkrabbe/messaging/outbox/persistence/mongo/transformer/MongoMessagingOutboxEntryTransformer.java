package de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.transformer;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryId;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntryLock;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;

import java.util.Optional;

public class MongoMessagingOutboxEntryTransformer {

    public static MongoMessagingOutboxEntry toMongoMessagingOutboxEntry(MessagingOutboxEntry entry) {
        var result = new MongoMessagingOutboxEntry();

        result.id = entry.getId().getValue();
        result.targetType = MongoMessageTargetTypeTransformer.toMongoMessageTargetType(entry.getTarget().getType());
        result.exchange = entry.getTarget()
                .getExchange()
                .map(ExchangeTarget::getName)
                .orElse(null);
        result.routingKey = entry.getRoutingKey().asString();
        result.payload = entry.getPayload();
        result.createdAt = entry.getCreatedAt();
        result.lockedAt = entry.getLockedAt().orElse(null);
        result.lock = entry.getLock()
                .map(MessagingOutboxEntryLock::getValue)
                .orElse(null);
        result.acknowledgedAt = entry.getAcknowledgedAt().orElse(null);
        result.failedAt = entry.getFailedAt().orElse(null);
        result.retryCount = entry.getRetryCount();

        return result;
    }

    public static MessagingOutboxEntry toMessagingOutboxEntry(MongoMessagingOutboxEntry entry) {
        var id = MessagingOutboxEntryId.of(entry.id);
        var targetType = MongoMessageTargetTypeTransformer.toMessageTargetType(entry.targetType);
        var target = switch (targetType) {
            case EXCHANGE -> MessageTarget.exchange(ExchangeTarget.of(entry.exchange));
        };
        var routingKey = RoutingKey.parse(entry.routingKey);
        var payload = entry.payload;
        var createdAt = entry.createdAt;
        var lockedAt = entry.lockedAt;
        var lock = Optional.ofNullable(entry.lock)
                .map(MessagingOutboxEntryLock::of)
                .orElse(null);
        var acknowledgedAt = entry.acknowledgedAt;
        var failedAt = entry.failedAt;
        var retryCount = entry.retryCount;

        return MessagingOutboxEntry.of(
                id,
                target,
                routingKey,
                payload,
                createdAt,
                lockedAt,
                lock,
                acknowledgedAt,
                failedAt,
                retryCount
        );
    }

}
