package de.bennyboer.kicherkrabbe.products.api.responses;

import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryLinksResponse {

    long total;

    List<LinkDTO> links;

}
