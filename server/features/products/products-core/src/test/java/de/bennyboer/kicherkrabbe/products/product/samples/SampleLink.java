package de.bennyboer.kicherkrabbe.products.product.samples;

import de.bennyboer.kicherkrabbe.products.product.Link;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkName;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import lombok.Builder;

@Builder
public class SampleLink {

    @Builder.Default
    private LinkType type = LinkType.PATTERN;

    @Builder.Default
    private String id = "LINK_ID";

    @Builder.Default
    private String name = "Sample Link";

    public Link toValue() {
        return Link.of(type, LinkId.of(id), LinkName.of(name));
    }

}
