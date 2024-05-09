package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup;

import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.mongo.MongoFabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.mongo.MongoLookupFabricType;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoFabricTypeLookupRepoTest extends FabricTypeLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoFabricTypeLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected FabricTypeLookupRepo createRepo() {
        return new MongoFabricTypeLookupRepo("fabric_types_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupFabricType.class)
                .inCollection("fabric_types_lookup")
                .all()
                .block();
    }

}
