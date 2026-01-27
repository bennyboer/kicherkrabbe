package de.bennyboer.kicherkrabbe.patterns.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PatternVariantDTO {

    String name;

    Set<PricedSizeRangeDTO> pricedSizeRanges;

}
