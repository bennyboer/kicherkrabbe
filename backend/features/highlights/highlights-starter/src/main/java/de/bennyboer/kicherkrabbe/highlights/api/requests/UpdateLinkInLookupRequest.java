package de.bennyboer.kicherkrabbe.highlights.api.requests;

import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateLinkInLookupRequest {

    LinkDTO link;

    long version;

}
