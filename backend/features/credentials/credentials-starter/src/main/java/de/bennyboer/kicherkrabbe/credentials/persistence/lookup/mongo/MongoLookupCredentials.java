package de.bennyboer.kicherkrabbe.credentials.persistence.lookup.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupCredentials {

    @MongoId
    String id;

    @Nullable
    Long version;

    String name;

    String userId;

}
