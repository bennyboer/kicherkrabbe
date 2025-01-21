package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.product.Links;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class LinksTransformer {

    public static Links toInternal(List<LinkDTO> links) {
        notNull(links, "Links must be given");

        return Links.of(LinkTransformer.toInternal(links));
    }

    public static List<LinkDTO> toApi(Links links) {
        return LinkTransformer.toApi(links.getLinks());
    }

}
