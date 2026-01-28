package de.bennyboer.kicherkrabbe.products.api.requests;

import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class RemoveLinkFromLookupRequest {

    LinkTypeDTO linkType;

    String linkId;

}
