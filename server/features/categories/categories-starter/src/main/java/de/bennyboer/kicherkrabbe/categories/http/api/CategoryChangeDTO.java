package de.bennyboer.kicherkrabbe.categories.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CategoryChangeDTO {

    String type;

    List<String> affected;

    Map<String, Object> payload;

}
