package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdatePatternAttributionRequest {

    PatternAttributionDTO attribution;

    long version;

}
