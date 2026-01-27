package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoFabricTypeAvailability {

    String fabricTypeId;

    boolean inStock;

}
