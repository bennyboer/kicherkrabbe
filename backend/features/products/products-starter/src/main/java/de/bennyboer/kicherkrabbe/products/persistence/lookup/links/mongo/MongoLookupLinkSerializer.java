package de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LookupLink;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkName;
import de.bennyboer.kicherkrabbe.products.product.LinkType;

public class MongoLookupLinkSerializer implements ReadModelSerializer<LookupLink, MongoLookupLink> {

    @Override
    public MongoLookupLink serialize(LookupLink link) {
        var result = new MongoLookupLink();

        result.id = link.getId();
        result.type = toMongoLinkType(link.getType());
        result.linkId = link.getLinkId().getValue();
        result.name = link.getName().getValue();

        return result;
    }

    @Override
    public LookupLink deserialize(MongoLookupLink link) {
        var id = link.id;
        var type = toInternalLinkType(link.type);
        var linkId = LinkId.of(link.linkId);
        var name = LinkName.of(link.name);

        return LookupLink.of(id, type, linkId, name);
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
