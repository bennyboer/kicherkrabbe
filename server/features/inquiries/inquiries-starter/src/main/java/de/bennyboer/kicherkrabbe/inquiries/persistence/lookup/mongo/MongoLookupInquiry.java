package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupInquiry {

    @MongoId
    String id;

    long version;

    String requestId;

    MongoSender sender;

    String subject;

    String message;

    MongoFingerprint fingerprint;

    Instant createdAt;

}
