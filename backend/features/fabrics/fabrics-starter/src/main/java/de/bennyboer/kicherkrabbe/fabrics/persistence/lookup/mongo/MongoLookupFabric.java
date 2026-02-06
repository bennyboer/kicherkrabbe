package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo;

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
public class MongoLookupFabric {

    @MongoId
    String id;

    long version;

    String name;

    String alias;

    String imageId;

    Set<String> colorIds;

    Set<String> topicIds;

    Set<MongoFabricTypeAvailability> availability;

    boolean published;

    boolean featured;

    Instant createdAt;

}
