package de.bennyboer.kicherkrabbe.highlights.api.responses;

import de.bennyboer.kicherkrabbe.highlights.api.HighlightDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryHighlightsResponse {

    long skip;

    long limit;

    long total;

    List<HighlightDTO> highlights;

}
