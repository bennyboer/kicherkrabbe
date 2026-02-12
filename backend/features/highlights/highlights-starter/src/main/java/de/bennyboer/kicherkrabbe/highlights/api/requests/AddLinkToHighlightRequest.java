package de.bennyboer.kicherkrabbe.highlights.api.requests;

import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class AddLinkToHighlightRequest {

    long version;

    LinkTypeDTO linkType;

    String linkId;

}
