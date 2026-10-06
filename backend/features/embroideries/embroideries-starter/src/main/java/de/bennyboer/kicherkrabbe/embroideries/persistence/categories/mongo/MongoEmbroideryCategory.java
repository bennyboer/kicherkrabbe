package de.bennyboer.kicherkrabbe.embroideries.persistence.categories.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoEmbroideryCategory {

    @MongoId
    String id;

    String name;

}
