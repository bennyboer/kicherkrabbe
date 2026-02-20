package de.bennyboer.kicherkrabbe.offers.api.requests;

import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;

import java.util.List;

public class CreateOfferRequest {

    public String productId;

    public List<String> imageIds;

    public NotesDTO notes;

    public MoneyDTO price;

}
