package de.bennyboer.kicherkrabbe.patterns.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdatePatternNumberRequest {

    String number;

    long version;

}
