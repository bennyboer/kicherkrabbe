package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPatternsRequest {

    String searchTerm;

    Set<String> categories;

    long skip;

    long limit;

}
