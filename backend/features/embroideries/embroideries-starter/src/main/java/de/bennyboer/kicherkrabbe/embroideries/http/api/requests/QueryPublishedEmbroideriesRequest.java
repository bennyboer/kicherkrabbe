package de.bennyboer.kicherkrabbe.embroideries.http.api.requests;

import de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideriesSortDTO;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryPublishedEmbroideriesRequest {

    String searchTerm;

    Set<String> categories;

    EmbroideriesSortDTO sort;

    long skip;

    long limit;

}
