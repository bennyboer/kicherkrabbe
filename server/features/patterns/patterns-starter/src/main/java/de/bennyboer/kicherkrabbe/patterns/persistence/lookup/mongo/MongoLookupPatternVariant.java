package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupPatternVariant {

    String name;

    Set<MongoLookupPricedSizeRange> pricedSizeRanges;

}
