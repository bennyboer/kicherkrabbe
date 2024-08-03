package de.bennyboer.kicherkrabbe.patterns.http.api.responses;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPatternsResponse {

    long skip;

    long limit;

    long total;

    List<PatternDTO> patterns;

}
