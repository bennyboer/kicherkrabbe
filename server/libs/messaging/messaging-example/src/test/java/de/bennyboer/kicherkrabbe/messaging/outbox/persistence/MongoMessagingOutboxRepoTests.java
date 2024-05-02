package de.bennyboer.kicherkrabbe.messaging.outbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.transformer.MongoMessagingOutboxEntryTransformer;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.List;

@MongoTest
public class MongoMessagingOutboxRepoTests extends MessagingOutboxRepoTests {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoMessagingOutboxRepoTests(ReactiveMongoTemplate template) {
        super();

        this.template = template;
    }

    @Override
    protected MessagingOutboxRepo createRepo() {
        return new MongoMessagingOutboxRepo("test", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoMessagingOutboxEntry.class)
                .inCollection("test")
                .all()
                .block();
    }

    @Override
    protected List<MessagingOutboxEntry> findEntries() {
        return template.findAll(MongoMessagingOutboxEntry.class, "test")
                .map(MongoMessagingOutboxEntryTransformer::toMessagingOutboxEntry)
                .collectList()
                .block();
    }

}

