package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupMail {

    @MongoId
    String id;

    long version;

    MongoOrigin origin;

    MongoSender sender;

    String subject;

    String content;

    Instant receivedAt;

    MongoStatus status;

    @Nullable
    Instant readAt;

}
