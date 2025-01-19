package de.bennyboer.kicherkrabbe.products.api.responses;

import de.bennyboer.kicherkrabbe.products.api.ProductDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryProductsResponse {

    long total;

    List<ProductDTO> products;

}
