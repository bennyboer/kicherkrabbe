package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.testing.persistence.MongoTest;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.mongo.MongoUserLookup;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.mongo.MongoUserLookupRepo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoUserLookupRepoTest extends UserLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoUserLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected UserLookupRepo createRepo() {
        return new MongoUserLookupRepo("users_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoUserLookup.class)
                .inCollection("users_lookup")
                .all()
                .block();
    }

}
