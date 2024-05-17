package de.bennyboer.kicherkrabbe.fabrics.persistence.topics;

import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.mongo.MongoTopic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.mongo.MongoTopicRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoTopicRepoTest extends TopicRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoTopicRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected TopicRepo createRepo() {
        return new MongoTopicRepo("fabrics_topics", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoTopic.class)
                .inCollection("fabrics_topics")
                .all()
                .block();
    }

}
