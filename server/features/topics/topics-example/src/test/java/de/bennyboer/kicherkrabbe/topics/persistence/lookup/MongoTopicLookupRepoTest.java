package de.bennyboer.kicherkrabbe.topics.persistence.lookup;

import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.mongo.MongoLookupTopic;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.mongo.MongoTopicLookupRepo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoTopicLookupRepoTest extends TopicLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoTopicLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected TopicLookupRepo createRepo() {
        return new MongoTopicLookupRepo("topics_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupTopic.class)
                .inCollection("topics_lookup")
                .all()
                .block();
    }

}
