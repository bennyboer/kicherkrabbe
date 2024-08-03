package de.bennyboer.kicherkrabbe.patterns.http.api.responses;

import de.bennyboer.kicherkrabbe.patterns.http.api.PublishedPatternDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedPatternsResponse {

    long skip;

    long limit;

    long total;

    List<PublishedPatternDTO> patterns;

}
