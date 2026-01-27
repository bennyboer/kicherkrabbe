package de.bennyboer.kicherkrabbe.fabrics.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class FabricsAvailabilityFilterDTO {

    boolean active;

    boolean inStock;

}
