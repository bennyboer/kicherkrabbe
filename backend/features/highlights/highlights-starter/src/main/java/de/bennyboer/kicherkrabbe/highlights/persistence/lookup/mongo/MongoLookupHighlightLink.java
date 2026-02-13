package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoLookupHighlightLink {

    String type;

    String id;

    String name;

}
