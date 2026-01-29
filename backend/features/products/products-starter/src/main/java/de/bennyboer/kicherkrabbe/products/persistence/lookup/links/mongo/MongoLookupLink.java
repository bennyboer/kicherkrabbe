package de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo;

import jakarta.annotation.Nullable;
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

    @Nullable
    Long version;

    MongoLinkType type;

    String linkId;

    String name;

}
