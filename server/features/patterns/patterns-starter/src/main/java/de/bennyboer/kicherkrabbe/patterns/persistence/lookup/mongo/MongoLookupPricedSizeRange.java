package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupPricedSizeRange {

    long from;

    @Nullable
    Long to;

    @Nullable
    String unit;

    MongoLookupPrice price;

}
