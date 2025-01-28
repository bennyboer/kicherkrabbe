package de.bennyboer.kicherkrabbe.products.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.products.product.Link;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkName;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupLink {

    String id;

    LinkType type;

    LinkId linkId;

    LinkName name;

    public static LookupLink of(
            String id,
            LinkType type,
            LinkId linkId,
            LinkName name
    ) {
        notNull(id, "ID must be given");
        notNull(type, "Type must be given");
        notNull(linkId, "Link ID must be given");
        notNull(name, "Name must be given");

        return new LookupLink(id, type, linkId, name);
    }

    public static LookupLink create(Link link) {
        String id = randomUUID().toString();

        return LookupLink.of(
                id,
                link.getType(),
                link.getId(),
                link.getName()
        );
    }

    public Link toLink() {
        return Link.of(type, linkId, name);
    }

}
