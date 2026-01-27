package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdatePatternExtrasRequest {

    List<PatternExtraDTO> extras;

    long version;

}
