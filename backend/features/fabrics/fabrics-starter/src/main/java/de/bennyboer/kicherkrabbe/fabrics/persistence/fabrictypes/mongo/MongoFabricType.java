package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoFabricType {

    @MongoId
    String id;

    String name;

}
