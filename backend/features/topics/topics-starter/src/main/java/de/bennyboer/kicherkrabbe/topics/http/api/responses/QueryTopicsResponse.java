package de.bennyboer.kicherkrabbe.topics.http.api.responses;

import de.bennyboer.kicherkrabbe.topics.http.api.TopicDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryTopicsResponse {

    long skip;

    long limit;

    long total;

    List<TopicDTO> topics;

}
