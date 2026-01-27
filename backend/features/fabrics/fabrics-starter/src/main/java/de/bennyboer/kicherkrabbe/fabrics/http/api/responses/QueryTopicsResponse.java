package de.bennyboer.kicherkrabbe.fabrics.http.api.responses;

import de.bennyboer.kicherkrabbe.fabrics.http.api.TopicDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryTopicsResponse {

    public List<TopicDTO> topics;

}
