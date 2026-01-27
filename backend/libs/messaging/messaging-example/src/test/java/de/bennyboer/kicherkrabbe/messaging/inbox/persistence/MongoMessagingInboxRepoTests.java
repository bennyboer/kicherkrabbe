package de.bennyboer.kicherkrabbe.messaging.inbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessage;
import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessageId;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.mongo.MongoIncomingMessage;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.mongo.MongoMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.List;

@MongoTest
public class MongoMessagingInboxRepoTests extends MessagingInboxRepoTests {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoMessagingInboxRepoTests(ReactiveMongoTemplate template) {
        super();

        this.template = template;
    }

    @Override
    protected MessagingInboxRepo createRepo() {
        return new MongoMessagingInboxRepo("test-inbox", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoIncomingMessage.class)
                .inCollection("test-inbox")
                .all()
                .block();
    }

    @Override
    protected List<IncomingMessage> findAll() {
        return template.findAll(MongoIncomingMessage.class, "test-inbox")
                .map(msg -> IncomingMessage.of(IncomingMessageId.of(msg.id), msg.receivedAt))
                .collectList()
                .block();
    }

}

