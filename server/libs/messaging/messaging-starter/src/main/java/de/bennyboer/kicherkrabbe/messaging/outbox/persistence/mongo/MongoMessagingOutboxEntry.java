package de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.Map;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoMessagingOutboxEntry {

    @MongoId
    String id;

    MongoMessageTargetType targetType;

    @Nullable
    String exchange;

    String routingKey;

    Map<String, Object> payload;

    Instant createdAt;

    @Nullable
    Instant lockedAt;

    @Nullable
    String lock;

    @Nullable
    Instant acknowledgedAt;

    @Nullable
    Instant failedAt;

    int retryCount;

}
