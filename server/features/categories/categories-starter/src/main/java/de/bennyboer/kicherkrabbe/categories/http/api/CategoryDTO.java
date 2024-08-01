package de.bennyboer.kicherkrabbe.categories.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CategoryDTO {

    String id;

    long version;

    String name;

    CategoryGroupDTO group;

    Instant createdAt;

}
