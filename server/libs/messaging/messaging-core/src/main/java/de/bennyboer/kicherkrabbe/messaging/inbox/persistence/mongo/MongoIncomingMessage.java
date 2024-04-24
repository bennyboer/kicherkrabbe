package de.bennyboer.kicherkrabbe.messaging.inbox.persistence.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoIncomingMessage {

    @MongoId
    String id;

    Instant receivedAt;

}
