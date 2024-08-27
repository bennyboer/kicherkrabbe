package de.bennyboer.kicherkrabbe.patterns.http.api;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PatternDTO {

    String id;

    long version;

    boolean published;

    String name;

    @Nullable
    String description;

    PatternAttributionDTO attribution;

    Set<String> categories;

    List<String> images;

    List<PatternVariantDTO> variants;

    List<PatternExtraDTO> extras;

    Instant createdAt;

}
