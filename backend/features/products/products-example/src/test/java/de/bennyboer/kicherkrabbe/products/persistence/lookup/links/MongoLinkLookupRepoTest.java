package de.bennyboer.kicherkrabbe.products.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo.MongoLinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo.MongoLookupLink;
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
        return new MongoLinkLookupRepo("products_links_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupLink.class)
                .inCollection("products_links_lookup")
                .all()
                .block();
    }

}
