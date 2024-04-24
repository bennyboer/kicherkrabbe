package de.bennyboer.kicherkrabbe.messaging.inbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessage;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;

import java.util.List;

public class InMemoryMessagingInboxRepoTests extends MessagingInboxRepoTests {

    @Override
    protected MessagingInboxRepo createRepo() {
        return new InMemoryMessagingInboxRepo();
    }

    @Override
    protected List<IncomingMessage> findAll() {
        return ((InMemoryMessagingInboxRepo) repo).findAll().collectList().block();
    }

}
