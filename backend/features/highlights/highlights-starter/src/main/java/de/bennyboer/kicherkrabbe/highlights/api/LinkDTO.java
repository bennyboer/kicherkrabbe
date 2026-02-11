package de.bennyboer.kicherkrabbe.highlights.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class LinkDTO {

    LinkTypeDTO type;

    String id;

    String name;

}
