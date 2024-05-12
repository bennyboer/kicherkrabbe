package de.bennyboer.kicherkrabbe.fabrics.http.api.requests;

import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsAvailabilityFilterDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedFabricsRequest {

    public String searchTerm;

    public Set<String> colorIds;

    public Set<String> topicIds;

    public FabricsAvailabilityFilterDTO availability;

    public FabricsSortDTO sort;

    public long skip;

    public long limit;

}
