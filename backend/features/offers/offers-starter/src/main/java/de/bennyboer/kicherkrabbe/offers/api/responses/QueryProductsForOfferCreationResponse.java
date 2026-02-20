package de.bennyboer.kicherkrabbe.offers.api.responses;

import de.bennyboer.kicherkrabbe.offers.api.ProductForOfferCreationDTO;

import java.util.List;

public class QueryProductsForOfferCreationResponse {

    public List<ProductForOfferCreationDTO> products;

    public long total;

    public long skip;

    public long limit;

}
