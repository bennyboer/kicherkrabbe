package de.bennyboer.kicherkrabbe.offers.api;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class OfferDTO {

    public String id;

    public long version;

    public String title;

    public String size;

    public Set<String> categoryIds;

    public ProductDTO product;

    public List<String> imageIds;

    public Set<LinkDTO> links;

    public Set<FabricCompositionItemDTO> fabricCompositionItems;

    public PricingDTO pricing;

    public NotesDTO notes;

    public boolean published;

    public boolean reserved;

    public Instant createdAt;

    public Instant archivedAt;

}
