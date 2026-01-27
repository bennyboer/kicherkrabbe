package de.bennyboer.kicherkrabbe.messaging.outbox.persistence;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory.InMemoryMessagingOutboxRepo;

import java.util.List;

public class InMemoryMessagingOutboxRepoTests extends MessagingOutboxRepoTests {

    @Override
    protected MessagingOutboxRepo createRepo() {
        return new InMemoryMessagingOutboxRepo();
    }

    @Override
    protected List<MessagingOutboxEntry> findEntries() {
        return ((InMemoryMessagingOutboxRepo) repo).findAll().collectList().block();
    }

}
