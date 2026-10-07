package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupEmbroidery {

    @MongoId
    String id;

    long version;

    boolean published;

    boolean featured;

    String name;

    String alias;

    String image;

    Set<String> categories;

    Instant createdAt;

}
