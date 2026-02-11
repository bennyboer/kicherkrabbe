package de.bennyboer.kicherkrabbe.highlights.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateHighlightSortOrderRequest {

    long version;

    long sortOrder;

}
