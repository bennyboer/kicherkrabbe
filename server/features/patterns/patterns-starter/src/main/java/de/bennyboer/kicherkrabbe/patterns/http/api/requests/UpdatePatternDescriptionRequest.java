package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdatePatternDescriptionRequest {

    @Nullable
    String description;

    long version;

}
