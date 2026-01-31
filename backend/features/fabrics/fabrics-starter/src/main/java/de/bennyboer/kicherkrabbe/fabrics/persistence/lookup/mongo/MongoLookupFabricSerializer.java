package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.fabrics.*;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabric;

import java.util.stream.Collectors;

public class MongoLookupFabricSerializer implements ReadModelSerializer<LookupFabric, MongoLookupFabric> {

    @Override
    public MongoLookupFabric serialize(LookupFabric readModel) {
        var result = new MongoLookupFabric();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.name = readModel.getName().getValue();
        result.imageId = readModel.getImage().getValue();
        result.colorIds = readModel.getColors()
                .stream()
                .map(ColorId::getValue)
                .collect(Collectors.toSet());
        result.topicIds = readModel.getTopics()
                .stream()
                .map(TopicId::getValue)
                .collect(Collectors.toSet());
        result.availability = readModel.getAvailability()
                .stream()
                .map(this::toMongoFabricTypeAvailability)
                .collect(Collectors.toSet());
        result.published = readModel.isPublished();
        result.featured = readModel.isFeatured();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupFabric deserialize(MongoLookupFabric serialized) {
        var id = FabricId.of(serialized.id);
        var version = Version.of(serialized.version);
        var name = FabricName.of(serialized.name);
        var image = ImageId.of(serialized.imageId);
        var colors = serialized.colorIds
                .stream()
                .map(ColorId::of)
                .collect(Collectors.toSet());
        var topics = serialized.topicIds
                .stream()
                .map(TopicId::of)
                .collect(Collectors.toSet());
        var availability = serialized.availability
                .stream()
                .map(this::toFabricTypeAvailability)
                .collect(Collectors.toSet());
        var published = serialized.published;
        var featured = serialized.featured;
        var createdAt = serialized.createdAt;

        return LookupFabric.of(
                id,
                version,
                name,
                image,
                colors,
                topics,
                availability,
                published,
                featured,
                createdAt
        );
    }

    private MongoFabricTypeAvailability toMongoFabricTypeAvailability(FabricTypeAvailability availability) {
        var result = new MongoFabricTypeAvailability();

        result.fabricTypeId = availability.getTypeId().getValue();
        result.inStock = availability.isInStock();

        return result;
    }

    private FabricTypeAvailability toFabricTypeAvailability(MongoFabricTypeAvailability availability) {
        var typeId = FabricTypeId.of(availability.fabricTypeId);
        var inStock = availability.inStock;

        return FabricTypeAvailability.of(
                typeId,
                inStock
        );
    }

}
