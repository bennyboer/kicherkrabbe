package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo.MongoProductForOfferLookup;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo.MongoProductForOfferLookupRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoProductForOfferLookupRepoTest extends ProductForOfferLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoProductForOfferLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected ProductForOfferLookupRepo createRepo() {
        return new MongoProductForOfferLookupRepo(template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoProductForOfferLookup.class)
                .inCollection("offers_product_lookup")
                .all()
                .block();
    }

}
