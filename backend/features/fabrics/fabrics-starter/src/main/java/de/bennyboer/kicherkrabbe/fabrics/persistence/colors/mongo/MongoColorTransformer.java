package de.bennyboer.kicherkrabbe.fabrics.persistence.colors.mongo;

import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorName;

public class MongoColorTransformer {

    public static MongoColor toMongo(Color color) {
        var result = new MongoColor();

        result.id = color.getId().getValue();
        result.name = color.getName().getValue();
        result.red = color.getRed();
        result.green = color.getGreen();
        result.blue = color.getBlue();

        return result;
    }

    public static Color fromMongo(MongoColor color) {
        ColorId id = ColorId.of(color.id);
        ColorName name = ColorName.of(color.name);
        int red = color.red;
        int green = color.green;
        int blue = color.blue;

        return Color.of(id, name, red, green, blue);
    }

}
