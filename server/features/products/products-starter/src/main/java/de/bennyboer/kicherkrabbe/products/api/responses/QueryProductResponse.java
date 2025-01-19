package de.bennyboer.kicherkrabbe.products.api.responses;

import de.bennyboer.kicherkrabbe.products.api.ProductDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryProductResponse {

    ProductDTO product;

}
