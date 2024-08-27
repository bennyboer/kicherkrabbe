package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupPattern {

    @MongoId
    String id;

    long version;

    boolean published;

    String name;

    @Nullable
    String description;

    String alias;

    MongoLookupPatternAttribution attribution;

    Set<String> categories;

    List<String> images;

    List<MongoLookupPatternVariant> variants;

    List<MongoLookupPatternExtra> extras;

    Instant createdAt;

}
