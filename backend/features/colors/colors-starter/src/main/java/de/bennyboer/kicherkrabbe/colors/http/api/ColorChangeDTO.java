package de.bennyboer.kicherkrabbe.colors.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class ColorChangeDTO {

    String type;

    List<String> affected;

    Map<String, Object> payload;

}
