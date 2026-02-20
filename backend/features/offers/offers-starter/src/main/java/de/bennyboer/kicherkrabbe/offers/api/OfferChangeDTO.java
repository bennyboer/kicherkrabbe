package de.bennyboer.kicherkrabbe.offers.api;

import java.util.List;
import java.util.Map;

public class OfferChangeDTO {

    public String type;

    public List<String> affected;

    public Map<String, Object> payload;

}
