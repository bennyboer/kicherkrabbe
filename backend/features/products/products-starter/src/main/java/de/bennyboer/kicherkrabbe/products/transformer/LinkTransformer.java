package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.product.Link;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkName;
import de.bennyboer.kicherkrabbe.products.product.LinkType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class LinkTransformer {

    public static Set<Link> toInternal(Collection<LinkDTO> links) {
        notNull(links, "Links must be given");

        return links.stream()
                .map(LinkTransformer::toInternal)
                .collect(Collectors.toSet());
    }

    public static Link toInternal(LinkDTO link) {
        notNull(link, "Link must be given");

        LinkType type = LinkTypeTransformer.toInternal(link.type);
        var id = LinkId.of(link.id);
        var name = LinkName.of(link.name);

        return Link.of(type, id, name);
    }

    public static List<LinkDTO> toApi(Collection<Link> links) {
        return links.stream()
                .map(LinkTransformer::toApi)
                .collect(Collectors.toList());
    }

    public static LinkDTO toApi(Link link) {
        var result = new LinkDTO();

        result.type = LinkTypeTransformer.toApi(link.getType());
        result.id = link.getId().getValue();
        result.name = link.getName().getValue();

        return result;
    }

}
