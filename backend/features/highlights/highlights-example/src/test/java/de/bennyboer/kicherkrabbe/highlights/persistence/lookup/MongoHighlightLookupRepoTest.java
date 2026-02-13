package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo.MongoHighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo.MongoLookupHighlight;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoHighlightLookupRepoTest extends HighlightLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoHighlightLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected HighlightLookupRepo createRepo() {
        return new MongoHighlightLookupRepo("highlights_lookup_test", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupHighlight.class)
                .inCollection("highlights_lookup_test")
                .all()
                .block();
    }

}
