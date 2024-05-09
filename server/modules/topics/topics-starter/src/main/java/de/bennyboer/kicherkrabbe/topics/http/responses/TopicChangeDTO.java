package de.bennyboer.kicherkrabbe.topics.http.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class TopicChangeDTO {

    String type;

    List<String> affected;

    Map<String, Object> payload;

}
