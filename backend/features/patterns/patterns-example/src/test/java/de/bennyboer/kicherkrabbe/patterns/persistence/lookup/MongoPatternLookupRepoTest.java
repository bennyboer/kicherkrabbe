package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo.MongoLookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo.MongoPatternLookupRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoPatternLookupRepoTest extends PatternLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoPatternLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected PatternLookupRepo createRepo() {
        return new MongoPatternLookupRepo("patterns_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupPattern.class)
                .inCollection("patterns_lookup")
                .all()
                .block();
    }

}
