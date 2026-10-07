package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;

import java.util.stream.Collectors;

public class MongoLookupEmbroiderySerializer implements ReadModelSerializer<LookupEmbroidery, MongoLookupEmbroidery> {

    @Override
    public MongoLookupEmbroidery serialize(LookupEmbroidery readModel) {
        var result = new MongoLookupEmbroidery();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.published = readModel.isPublished();
        result.featured = readModel.isFeatured();
        result.name = readModel.getName().getValue();
        result.alias = readModel.getAlias().getValue();
        result.image = readModel.getImage().getValue();
        result.categories = readModel.getCategories()
                .stream()
                .map(EmbroideryCategoryId::getValue)
                .collect(Collectors.toSet());
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupEmbroidery deserialize(MongoLookupEmbroidery serialized) {
        var id = EmbroideryId.of(serialized.id);
        var version = Version.of(serialized.version);
        var name = EmbroideryName.of(serialized.name);
        var alias = EmbroideryAlias.of(serialized.alias);
        var image = ImageId.of(serialized.image);
        var categories = serialized.categories
                .stream()
                .map(EmbroideryCategoryId::of)
                .collect(Collectors.toSet());

        return LookupEmbroidery.of(
                id,
                version,
                serialized.published,
                serialized.featured,
                name,
                alias,
                image,
                categories,
                serialized.createdAt
        );
    }

}
