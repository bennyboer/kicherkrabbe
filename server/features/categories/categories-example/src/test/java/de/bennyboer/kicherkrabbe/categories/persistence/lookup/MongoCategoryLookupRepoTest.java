package de.bennyboer.kicherkrabbe.categories.persistence.lookup;

import de.bennyboer.kicherkrabbe.categories.persistence.lookup.mongo.MongoCategoryLookupRepo;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.mongo.MongoLookupCategory;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoCategoryLookupRepoTest extends CategoryLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoCategoryLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected CategoryLookupRepo createRepo() {
        return new MongoCategoryLookupRepo("categories_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupCategory.class)
                .inCollection("categories_lookup")
                .all()
                .block();
    }

}
