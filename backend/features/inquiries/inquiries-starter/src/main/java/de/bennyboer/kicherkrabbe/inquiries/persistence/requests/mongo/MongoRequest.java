package de.bennyboer.kicherkrabbe.inquiries.persistence.requests.mongo;

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
public class MongoRequest {

    @MongoId
    String id;

    long version;

    String mail;

    @Nullable
    String ipAddress;

    Instant createdAt;

}
