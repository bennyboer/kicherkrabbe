package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdatePatternImagesRequest {

    List<String> images;

    long version;

}
