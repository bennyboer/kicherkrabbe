package de.bennyboer.kicherkrabbe.fabrics.http.api.responses;

import de.bennyboer.kicherkrabbe.fabrics.http.api.PublishedFabricDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedFabricsResponse {

    public long skip;

    public long limit;

    public long total;

    public List<PublishedFabricDTO> fabrics;

}
