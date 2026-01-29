package de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LookupLink;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkName;
import de.bennyboer.kicherkrabbe.products.product.LinkType;

import java.util.Optional;

public class MongoLookupLinkSerializer implements ReadModelSerializer<LookupLink, MongoLookupLink> {

    @Override
    public MongoLookupLink serialize(LookupLink link) {
        var result = new MongoLookupLink();

        result.id = link.getId();
        result.version = link.getVersion().getValue();
        result.type = toMongoLinkType(link.getType());
        result.linkId = link.getLinkId().getValue();
        result.name = link.getName().getValue();

        return result;
    }

    @Override
    public LookupLink deserialize(MongoLookupLink link) {
        var id = link.id;
        var version = Version.of(Optional.ofNullable(link.version).orElse(0L));
        var type = toInternalLinkType(link.type);
        var linkId = LinkId.of(link.linkId);
        var name = LinkName.of(link.name);

        return LookupLink.of(id, version, type, linkId, name);
    }

    private MongoLinkType toMongoLinkType(LinkType type) {
        return switch (type) {
            case PATTERN -> MongoLinkType.PATTERN;
            case FABRIC -> MongoLinkType.FABRIC;
        };
    }

    private LinkType toInternalLinkType(MongoLinkType type) {
        return switch (type) {
            case PATTERN -> LinkType.PATTERN;
            case FABRIC -> LinkType.FABRIC;
        };
    }

}
