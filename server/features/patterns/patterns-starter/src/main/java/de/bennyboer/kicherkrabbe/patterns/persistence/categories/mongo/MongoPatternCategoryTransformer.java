package de.bennyboer.kicherkrabbe.patterns.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryName;

public class MongoPatternCategoryTransformer {

    public static MongoPatternCategory toMongo(PatternCategory topic) {
        var result = new MongoPatternCategory();

        result.id = topic.getId().getValue();
        result.name = topic.getName().getValue();

        return result;
    }

    public static PatternCategory fromMongo(MongoPatternCategory topic) {
        PatternCategoryId id = PatternCategoryId.of(topic.id);
        PatternCategoryName name = PatternCategoryName.of(topic.name);

        return PatternCategory.of(id, name);
    }

}
