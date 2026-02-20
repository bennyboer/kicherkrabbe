package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoLookupOffer;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoOfferLookupRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoOfferLookupRepoTest extends OfferLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoOfferLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected OfferLookupRepo createRepo() {
        return new MongoOfferLookupRepo(template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupOffer.class)
                .inCollection("offers_lookup")
                .all()
                .block();
    }

}
