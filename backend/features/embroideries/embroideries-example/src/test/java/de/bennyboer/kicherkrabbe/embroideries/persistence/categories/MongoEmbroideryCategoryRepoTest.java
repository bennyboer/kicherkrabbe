package de.bennyboer.kicherkrabbe.embroideries.persistence.categories;

import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.mongo.MongoEmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.mongo.MongoEmbroideryCategoryRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoEmbroideryCategoryRepoTest extends EmbroideryCategoryRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoEmbroideryCategoryRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected EmbroideryCategoryRepo createRepo() {
        return new MongoEmbroideryCategoryRepo("embroideries_categories", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoEmbroideryCategory.class)
                .inCollection("embroideries_categories")
                .all()
                .block();
    }

}
