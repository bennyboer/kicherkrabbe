package de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupLink {

    @MongoId
    String id;

    MongoLinkType type;

    String linkId;

    String name;

}
