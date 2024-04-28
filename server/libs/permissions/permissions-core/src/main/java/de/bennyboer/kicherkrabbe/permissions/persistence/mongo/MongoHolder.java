package de.bennyboer.kicherkrabbe.permissions.persistence.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoHolder {

    String id;

    MongoHolderType type;

}
