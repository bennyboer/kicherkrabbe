package de.bennyboer.kicherkrabbe.assets.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupAsset {

    @MongoId
    String id;

    long version;

    String contentType;

    long fileSize;

    Instant createdAt;

}
