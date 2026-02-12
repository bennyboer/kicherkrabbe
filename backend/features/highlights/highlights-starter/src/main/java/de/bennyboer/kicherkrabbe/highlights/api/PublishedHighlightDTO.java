package de.bennyboer.kicherkrabbe.highlights.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PublishedHighlightDTO {

    String id;

    String imageId;

    List<LinkDTO> links;

}
