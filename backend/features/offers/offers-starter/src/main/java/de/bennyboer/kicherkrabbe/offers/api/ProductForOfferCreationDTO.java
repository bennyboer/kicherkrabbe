package de.bennyboer.kicherkrabbe.offers.api;

import java.util.List;
import java.util.Set;

public class ProductForOfferCreationDTO {

    public String id;

    public String number;

    public List<String> imageIds;

    public Set<LinkDTO> links;

    public Set<FabricCompositionItemDTO> fabricCompositionItems;

}
