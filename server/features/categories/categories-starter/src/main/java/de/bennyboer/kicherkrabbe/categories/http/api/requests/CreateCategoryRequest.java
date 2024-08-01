package de.bennyboer.kicherkrabbe.categories.http.api.requests;

import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreateCategoryRequest {

    String name;

    CategoryGroupDTO group;

}
