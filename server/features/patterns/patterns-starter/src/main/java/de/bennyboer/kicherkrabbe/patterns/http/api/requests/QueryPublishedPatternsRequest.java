package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternsSortDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedPatternsRequest {

    String searchTerm;

    Set<String> categories;

    PatternsSortDTO sort;

    long skip;

    long limit;

}
