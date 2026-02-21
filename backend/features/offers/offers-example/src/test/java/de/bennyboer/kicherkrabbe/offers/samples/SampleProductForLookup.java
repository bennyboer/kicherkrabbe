package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.offers.*;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public class SampleProductForLookup {

    @Builder.Default
    private String id = "PRODUCT_ID";

    @Builder.Default
    private ProductNumber number = ProductNumber.of("P-001");

    @Builder.Default
    private List<ImageId> images = List.of(ImageId.of("IMAGE_ID"));

    @Builder.Default
    private Set<Link> links = Set.of(
            Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern Name"))
    );

    @Builder.Default
    private Set<FabricCompositionItem> fabricCompositionItems = Set.of(
            FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
    );

    public String getId() {
        return id;
    }

    public ProductNumber getNumber() {
        return number;
    }

    public List<ImageId> getImages() {
        return images;
    }

    public Links getLinks() {
        return Links.of(links);
    }

    public FabricComposition getFabricComposition() {
        return FabricComposition.of(fabricCompositionItems);
    }

}
