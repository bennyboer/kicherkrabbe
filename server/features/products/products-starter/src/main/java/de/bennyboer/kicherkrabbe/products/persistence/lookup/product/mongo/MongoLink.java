package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLink {

    MongoLinkType type;

    String id;

    String name;

}
