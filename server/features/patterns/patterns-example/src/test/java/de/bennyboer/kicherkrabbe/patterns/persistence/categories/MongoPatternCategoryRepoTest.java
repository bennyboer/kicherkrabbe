package de.bennyboer.kicherkrabbe.patterns.persistence.categories;

import de.bennyboer.kicherkrabbe.patterns.persistence.categories.mongo.MongoPatternCategory;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.mongo.MongoPatternCategoryRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoPatternCategoryRepoTest extends PatternCategoryRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoPatternCategoryRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected PatternCategoryRepo createRepo() {
        return new MongoPatternCategoryRepo("patterns_categories", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoPatternCategory.class)
                .inCollection("patterns_categories")
                .all()
                .block();
    }

}
