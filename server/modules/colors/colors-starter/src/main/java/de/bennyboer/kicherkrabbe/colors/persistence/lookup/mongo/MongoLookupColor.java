package de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupColor {

    @MongoId
    String id;

    String name;

    int red;

    int green;

    int blue;

    Instant createdAt;

}
