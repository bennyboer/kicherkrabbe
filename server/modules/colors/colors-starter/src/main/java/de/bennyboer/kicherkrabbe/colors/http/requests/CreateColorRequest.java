package de.bennyboer.kicherkrabbe.colors.http.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreateColorRequest {

    String name;

    int red;

    int green;

    int blue;

}
