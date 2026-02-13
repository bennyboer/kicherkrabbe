package de.bennyboer.kicherkrabbe.highlights.samples;

import de.bennyboer.kicherkrabbe.highlights.Link;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkName;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
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
