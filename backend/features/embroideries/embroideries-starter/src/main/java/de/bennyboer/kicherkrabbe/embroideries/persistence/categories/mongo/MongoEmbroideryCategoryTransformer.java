package de.bennyboer.kicherkrabbe.embroideries.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryName;

public class MongoEmbroideryCategoryTransformer {

    public static MongoEmbroideryCategory toMongo(EmbroideryCategory category) {
        var result = new MongoEmbroideryCategory();

        result.id = category.getId().getValue();
        result.name = category.getName().getValue();

        return result;
    }

    public static EmbroideryCategory fromMongo(MongoEmbroideryCategory category) {
        var id = EmbroideryCategoryId.of(category.id);
        var name = EmbroideryCategoryName.of(category.name);

        return EmbroideryCategory.of(id, name);
    }

}
