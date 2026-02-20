package de.bennyboer.kicherkrabbe.offers.api.responses;

import de.bennyboer.kicherkrabbe.offers.api.PublishedOfferDTO;

import java.util.List;

public class QueryPublishedOffersResponse {

    public long skip;

    public long limit;

    public long total;

    public List<PublishedOfferDTO> offers;

}
