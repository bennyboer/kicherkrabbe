package de.bennyboer.kicherkrabbe.fabrics.http.api.responses;

import de.bennyboer.kicherkrabbe.fabrics.http.api.PublishedFabricDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedFabricResponse {

    public PublishedFabricDTO fabric;

}
