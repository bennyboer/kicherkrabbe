package de.bennyboer.kicherkrabbe.embroideries.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class EmbroideriesSortDTO {

    EmbroideriesSortPropertyDTO property;

    EmbroideriesSortDirectionDTO direction;

}
