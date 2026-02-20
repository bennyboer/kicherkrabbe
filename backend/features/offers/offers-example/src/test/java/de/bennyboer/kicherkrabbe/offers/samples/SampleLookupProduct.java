package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import lombok.Builder;

import java.util.Set;

@Builder
public class SampleLookupProduct {

    @Builder.Default
    private String id = "PRODUCT_ID";

    @Builder.Default
    private long version = 0L;

    @Builder.Default
    private String number = "P-001";

    @Builder.Default
    private Set<Link> links = Set.of(Link.of(LinkType.PATTERN, LinkId.of("LINK_ID"), LinkName.of("Link")));

    @Builder.Default
    private Set<FabricCompositionItem> fabricCompositionItems = Set.of(
            FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
    );

    public LookupProduct toModel() {
        return LookupProduct.of(
                ProductId.of(id),
                Version.of(version),
                ProductNumber.of(number),
                Links.of(links),
                FabricComposition.of(fabricCompositionItems)
        );
    }

}
