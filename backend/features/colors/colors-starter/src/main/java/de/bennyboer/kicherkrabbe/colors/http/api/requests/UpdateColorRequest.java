package de.bennyboer.kicherkrabbe.colors.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateColorRequest {

    String name;

    long version;

    int red;

    int green;

    int blue;

}
