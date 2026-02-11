package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.mongo.MongoLinkLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.mongo.MongoLookupLink;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoLinkLookupRepoTest extends LinkLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoLinkLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected LinkLookupRepo createRepo() {
        return new MongoLinkLookupRepo("highlights_links_lookup_test", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupLink.class)
                .inCollection("highlights_links_lookup_test")
                .all()
                .block();
    }

}
