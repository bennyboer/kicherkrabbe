package de.bennyboer.kicherkrabbe.colors.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class ColorDTO {

    String id;

    long version;

    String name;

    int red;

    int green;

    int blue;

    Instant createdAt;

}
