package de.bennyboer.kicherkrabbe.messaging.inbox.persistence.mongo;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessage;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.IncomingMessageAlreadySeenException;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class MongoMessagingInboxRepo implements MessagingInboxRepo {

    private final String collection;

    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Void> insert(IncomingMessage message) {
        MongoIncomingMessage msg = toMongoIncomingMessage(message);

        return template.insert(msg, collection)
                .onErrorMap(DuplicateKeyException.class, e -> new IncomingMessageAlreadySeenException())
                .then();
    }

    private MongoIncomingMessage toMongoIncomingMessage(IncomingMessage message) {
        var result = new MongoIncomingMessage();

        result.id = message.getId().getValue();
        result.receivedAt = message.getReceivedAt();

        return result;
    }

}
