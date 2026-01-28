package de.bennyboer.kicherkrabbe.messaging.inbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessage;
import reactor.core.publisher.Mono;

public interface MessagingInboxRepo {

    Mono<Void> insert(IncomingMessage message);

}
