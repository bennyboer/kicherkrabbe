package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupHighlight {

    @MongoId
    String id;

    long version;

    String imageId;

    List<MongoLookupHighlightLink> links;

    boolean published;

    long sortOrder;

    Instant createdAt;

}
