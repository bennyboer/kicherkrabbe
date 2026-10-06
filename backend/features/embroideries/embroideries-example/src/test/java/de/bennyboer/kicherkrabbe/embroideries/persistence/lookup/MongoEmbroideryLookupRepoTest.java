package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup;

import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.mongo.MongoEmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.mongo.MongoLookupEmbroidery;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoEmbroideryLookupRepoTest extends EmbroideryLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoEmbroideryLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected EmbroideryLookupRepo createRepo() {
        return new MongoEmbroideryLookupRepo("embroideries_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupEmbroidery.class)
                .inCollection("embroideries_lookup")
                .all()
                .block();
    }

}
