package de.bennyboer.kicherkrabbe.fabrics.http.api.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryFabricsRequest {

    public String searchTerm;

    public long skip;

    public long limit;

}
