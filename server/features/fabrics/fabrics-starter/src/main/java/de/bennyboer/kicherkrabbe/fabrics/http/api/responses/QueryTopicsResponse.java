package de.bennyboer.kicherkrabbe.fabrics.http.api.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryTopicsResponse {

    public List<String> topicIds;

}
