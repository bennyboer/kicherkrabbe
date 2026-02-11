package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.highlights.*;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlight;

import java.util.Set;
import java.util.stream.Collectors;

public class MongoLookupHighlightSerializer implements ReadModelSerializer<LookupHighlight, MongoLookupHighlight> {

    @Override
    public MongoLookupHighlight serialize(LookupHighlight readModel) {
        var result = new MongoLookupHighlight();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.imageId = readModel.getImageId().getValue();
        result.links = readModel.getLinks().getLinks().stream()
                .map(this::serializeLink)
                .toList();
        result.published = readModel.isPublished();
        result.sortOrder = readModel.getSortOrder();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupHighlight deserialize(MongoLookupHighlight serialized) {
        var id = HighlightId.of(serialized.id);
        var version = Version.of(serialized.version);
        var imageId = ImageId.of(serialized.imageId);
        Set<Link> linkSet = serialized.links.stream()
                .map(this::deserializeLink)
                .collect(Collectors.toSet());
        var links = Links.of(linkSet);
        var published = serialized.published;
        var sortOrder = serialized.sortOrder;
        var createdAt = serialized.createdAt;

        return LookupHighlight.of(
                id,
                version,
                imageId,
                links,
                published,
                sortOrder,
                createdAt
        );
    }

    private MongoLookupHighlightLink serializeLink(Link link) {
        var result = new MongoLookupHighlightLink();
        result.type = link.getType().name();
        result.id = link.getId().getValue();
        result.name = link.getName().getValue();
        return result;
    }

    private Link deserializeLink(MongoLookupHighlightLink link) {
        return Link.of(
                LinkType.valueOf(link.type),
                LinkId.of(link.id),
                LinkName.of(link.name)
        );
    }

}
