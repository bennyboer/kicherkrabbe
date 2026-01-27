package de.bennyboer.kicherkrabbe.patterns.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryName;

public class MongoPatternCategoryTransformer {

    public static MongoPatternCategory toMongo(PatternCategory category) {
        var result = new MongoPatternCategory();

        result.id = category.getId().getValue();
        result.name = category.getName().getValue();

        return result;
    }

    public static PatternCategory fromMongo(MongoPatternCategory category) {
        PatternCategoryId id = PatternCategoryId.of(category.id);
        PatternCategoryName name = PatternCategoryName.of(category.name);

        return PatternCategory.of(id, name);
    }

}
