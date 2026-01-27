package de.bennyboer.kicherkrabbe.fabrics.persistence.colors.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoColor {

    @MongoId
    String id;

    String name;

    int red;

    int green;

    int blue;

}
