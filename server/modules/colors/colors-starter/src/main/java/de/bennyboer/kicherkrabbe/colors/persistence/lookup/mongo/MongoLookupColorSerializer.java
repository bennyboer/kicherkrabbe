package de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.ColorName;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;

public class MongoLookupColorSerializer implements ReadModelSerializer<LookupColor, MongoLookupColor> {

    @Override
    public MongoLookupColor serialize(LookupColor readModel) {
        var result = new MongoLookupColor();

        result.id = readModel.getId().getValue();
        result.name = readModel.getName().getValue();
        result.red = readModel.getRed();
        result.green = readModel.getGreen();
        result.blue = readModel.getBlue();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupColor deserialize(MongoLookupColor serialized) {
        var id = ColorId.of(serialized.id);
        var name = ColorName.of(serialized.name);
        var red = serialized.red;
        var green = serialized.green;
        var blue = serialized.blue;
        var createdAt = serialized.createdAt;

        return LookupColor.of(
                id,
                name,
                red,
                green,
                blue,
                createdAt
        );
    }

}
