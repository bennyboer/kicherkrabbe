package de.bennyboer.kicherkrabbe.offers.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.offers.OfferCategory;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryName;

public class MongoOfferCategoryTransformer {

    public static MongoOfferCategory toMongo(OfferCategory category) {
        var result = new MongoOfferCategory();

        result.id = category.getId().getValue();
        result.name = category.getName().getValue();

        return result;
    }

    public static OfferCategory fromMongo(MongoOfferCategory category) {
        OfferCategoryId id = OfferCategoryId.of(category.id);
        OfferCategoryName name = OfferCategoryName.of(category.name);

        return OfferCategory.of(id, name);
    }

}
