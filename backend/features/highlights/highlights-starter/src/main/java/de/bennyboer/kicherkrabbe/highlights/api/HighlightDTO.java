package de.bennyboer.kicherkrabbe.highlights.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class HighlightDTO {

    String id;

    long version;

    String imageId;

    List<LinkDTO> links;

    boolean published;

    long sortOrder;

    Instant createdAt;

}
