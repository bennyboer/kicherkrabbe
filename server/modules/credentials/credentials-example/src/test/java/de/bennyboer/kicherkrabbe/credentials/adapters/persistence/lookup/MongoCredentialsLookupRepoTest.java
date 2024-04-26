package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.mongo.MongoCredentialsLookup;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.mongo.MongoCredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.testing.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoCredentialsLookupRepoTest extends CredentialsLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoCredentialsLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected CredentialsLookupRepo createRepo() {
        return new MongoCredentialsLookupRepo("credentials_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoCredentialsLookup.class)
                .inCollection("credentials_lookup")
                .all()
                .block();
    }

}
