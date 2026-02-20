package de.bennyboer.kicherkrabbe.offers.api;

import java.util.List;
import java.util.Set;

public class PublishedOfferDTO {

    public String id;

    public List<String> imageIds;

    public Set<LinkDTO> links;

    public Set<FabricCompositionItemDTO> fabricCompositionItems;

    public PricingDTO pricing;

    public NotesDTO notes;

}
