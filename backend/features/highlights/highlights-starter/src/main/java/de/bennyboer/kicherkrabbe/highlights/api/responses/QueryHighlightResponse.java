package de.bennyboer.kicherkrabbe.highlights.api.responses;

import de.bennyboer.kicherkrabbe.highlights.api.HighlightDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryHighlightResponse {

    HighlightDTO highlight;

}
