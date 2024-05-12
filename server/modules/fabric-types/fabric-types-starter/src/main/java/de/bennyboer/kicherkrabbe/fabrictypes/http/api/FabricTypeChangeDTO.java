package de.bennyboer.kicherkrabbe.fabrictypes.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class FabricTypeChangeDTO {

    String type;

    List<String> affected;

    Map<String, Object> payload;

}
