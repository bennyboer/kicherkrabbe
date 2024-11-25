package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import de.bennyboer.kicherkrabbe.patterns.http.api.PatternAttributionDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternExtraDTO;
import de.bennyboer.kicherkrabbe.patterns.http.api.PatternVariantDTO;
import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreatePatternRequest {

    String name;

    @Nullable // TODO Remove after all patterns have a number
    String number;

    @Nullable
    String description;

    PatternAttributionDTO attribution;

    Set<String> categories;

    List<String> images;

    List<PatternVariantDTO> variants;

    List<PatternExtraDTO> extras;

}
