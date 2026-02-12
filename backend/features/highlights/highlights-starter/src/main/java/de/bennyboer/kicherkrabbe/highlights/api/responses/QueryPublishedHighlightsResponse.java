package de.bennyboer.kicherkrabbe.highlights.api.responses;

import de.bennyboer.kicherkrabbe.highlights.api.PublishedHighlightDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedHighlightsResponse {

    List<PublishedHighlightDTO> highlights;

}
