package de.bennyboer.kicherkrabbe.offers.persistence.categories;

import de.bennyboer.kicherkrabbe.offers.persistence.categories.mongo.MongoOfferCategory;
import de.bennyboer.kicherkrabbe.offers.persistence.categories.mongo.MongoOfferCategoryRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoOfferCategoryRepoTest extends OfferCategoryRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoOfferCategoryRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected OfferCategoryRepo createRepo() {
        return new MongoOfferCategoryRepo(template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoOfferCategory.class)
                .inCollection("offers_categories")
                .all()
                .block();
    }

}
