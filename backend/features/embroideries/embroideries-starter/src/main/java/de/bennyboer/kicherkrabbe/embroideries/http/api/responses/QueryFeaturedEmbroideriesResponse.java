package de.bennyboer.kicherkrabbe.embroideries.http.api.responses;

import de.bennyboer.kicherkrabbe.embroideries.http.api.PublishedEmbroideryDTO;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryFeaturedEmbroideriesResponse {

    List<PublishedEmbroideryDTO> embroideries;

}
