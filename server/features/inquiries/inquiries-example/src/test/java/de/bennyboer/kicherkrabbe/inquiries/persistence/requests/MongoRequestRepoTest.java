package de.bennyboer.kicherkrabbe.inquiries.persistence.requests;

import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.mongo.MongoRequest;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.mongo.MongoRequestRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoRequestRepoTest extends RequestRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoRequestRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected RequestRepo createRepo() {
        return new MongoRequestRepo("inquiries_requests", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoRequest.class)
                .inCollection("inquiries_requests")
                .all()
                .block();
    }

}
