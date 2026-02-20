package de.bennyboer.kicherkrabbe.offers.api.responses;

import de.bennyboer.kicherkrabbe.offers.api.OfferDTO;

import java.util.List;

public class QueryOffersResponse {

    public long skip;

    public long limit;

    public long total;

    public List<OfferDTO> offers;

}
