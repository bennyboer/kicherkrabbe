package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo.MongoColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo.MongoLookupColor;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoColorLookupRepoTest extends ColorLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoColorLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected ColorLookupRepo createRepo() {
        return new MongoColorLookupRepo("colors_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupColor.class)
                .inCollection("colors_lookup")
                .all()
                .block();
    }

}
