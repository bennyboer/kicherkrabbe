package de.bennyboer.kicherkrabbe.categories.http.api.responses;

import de.bennyboer.kicherkrabbe.categories.http.api.CategoryDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryCategoriesResponse {

    long skip;

    long limit;

    long total;

    List<CategoryDTO> categories;

}
