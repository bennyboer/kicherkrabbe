package de.bennyboer.kicherkrabbe.embroideries.http.api.responses;

import de.bennyboer.kicherkrabbe.embroideries.http.api.EmbroideryDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryEmbroideryResponse {

    EmbroideryDTO embroidery;

}
