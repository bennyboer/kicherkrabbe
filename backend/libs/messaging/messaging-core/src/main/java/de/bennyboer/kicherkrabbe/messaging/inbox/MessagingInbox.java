package de.bennyboer.kicherkrabbe.messaging.inbox;

import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Clock;

@Slf4j
@AllArgsConstructor
public class MessagingInbox {

    private final MessagingInboxRepo repo;

    private final Clock clock;

    public Mono<Void> addMessage(IncomingMessageId id) {
        var message = IncomingMessage.of(id, clock.instant());
        return repo.insert(message);
    }

}
