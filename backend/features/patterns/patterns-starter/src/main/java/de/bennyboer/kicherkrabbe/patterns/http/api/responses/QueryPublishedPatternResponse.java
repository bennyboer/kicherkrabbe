package de.bennyboer.kicherkrabbe.patterns.http.api.responses;

import de.bennyboer.kicherkrabbe.patterns.http.api.PublishedPatternDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedPatternResponse {

    PublishedPatternDTO pattern;

}
