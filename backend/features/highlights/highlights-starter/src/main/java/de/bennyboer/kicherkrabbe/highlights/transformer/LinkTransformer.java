package de.bennyboer.kicherkrabbe.highlights.transformer;

import de.bennyboer.kicherkrabbe.highlights.Link;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;

public class LinkTransformer {

    public static LinkDTO toApi(Link link) {
        var result = new LinkDTO();

        result.type = LinkTypeTransformer.toApi(link.getType());
        result.id = link.getId().getValue();
        result.name = link.getName().getValue();

        return result;
    }

}
