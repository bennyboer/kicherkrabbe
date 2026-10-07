package de.bennyboer.kicherkrabbe.embroideries.http.api;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class EmbroideryChangeDTO {

    String type;

    List<String> affected;

    Map<String, Object> payload;

}
