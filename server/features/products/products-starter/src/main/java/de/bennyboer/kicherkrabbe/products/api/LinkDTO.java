package de.bennyboer.kicherkrabbe.products.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class LinkDTO {

    LinkTypeDTO type;

    String name;

    String id;

}
