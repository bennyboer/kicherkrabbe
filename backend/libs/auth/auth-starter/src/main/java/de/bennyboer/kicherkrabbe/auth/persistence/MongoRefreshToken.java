package de.bennyboer.kicherkrabbe.auth.persistence;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoRefreshToken {

    @MongoId
    String id;

    String tokenValue;

    String userId;

    String family;

    boolean used;

    Instant expiresAt;

    Instant createdAt;

}
