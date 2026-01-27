package de.bennyboer.kicherkrabbe.permissions.persistence.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoPermission {

    @MongoId
    String id;

    MongoHolder holder;

    String action;

    MongoResource resource;

    Instant createdAt;

}
