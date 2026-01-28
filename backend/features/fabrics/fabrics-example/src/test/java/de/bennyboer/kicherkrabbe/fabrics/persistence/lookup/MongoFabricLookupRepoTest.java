package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo.MongoFabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo.MongoLookupFabric;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoFabricLookupRepoTest extends FabricLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoFabricLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected FabricLookupRepo createRepo() {
        return new MongoFabricLookupRepo("fabrics_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupFabric.class)
                .inCollection("fabrics_lookup")
                .all()
                .block();
    }

}
