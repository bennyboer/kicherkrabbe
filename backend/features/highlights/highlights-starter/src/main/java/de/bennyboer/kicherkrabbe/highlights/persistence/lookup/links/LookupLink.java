package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.highlights.Link;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkName;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupLink implements VersionedReadModel<String> {

    String id;

    Version version;

    LinkType type;

    LinkId linkId;

    LinkName name;

    public static LookupLink of(
            String id,
            Version version,
            LinkType type,
            LinkId linkId,
            LinkName name
    ) {
        notNull(id, "ID must be given");
        notNull(version, "Version must be given");
        notNull(type, "Type must be given");
        notNull(linkId, "Link ID must be given");
        notNull(name, "Name must be given");

        return new LookupLink(id, version, type, linkId, name);
    }

    public static LookupLink create(Link link, Version version) {
        String id = "%s-%s".formatted(link.getType(), link.getId().getValue());

        return LookupLink.of(
                id,
                version,
                link.getType(),
                link.getId(),
                link.getName()
        );
    }

    public Link toLink() {
        return Link.of(type, linkId, name);
    }

}
