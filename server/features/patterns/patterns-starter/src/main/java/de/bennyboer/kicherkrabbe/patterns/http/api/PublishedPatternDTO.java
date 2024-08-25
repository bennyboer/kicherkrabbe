package de.bennyboer.kicherkrabbe.patterns.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PublishedPatternDTO {

    String id;

    String name;
    
    String alias;

    PatternAttributionDTO attribution;

    Set<String> categories;

    List<String> images;

    List<PatternVariantDTO> variants;

    List<PatternExtraDTO> extras;

}
