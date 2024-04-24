package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory.InMemoryMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.testing.persistence.MockReactiveTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
public class MessagingTestConfig {

    @Bean
    public MessagingInboxRepo messagingInboxRepo() {
        return new InMemoryMessagingInboxRepo(false);
    }

    @Bean
    public MessagingOutboxRepo messagingOutboxRepo() {
        return new InMemoryMessagingOutboxRepo();
    }

    @Bean
    public ReactiveTransactionManager transactionManager() {
        return new MockReactiveTransactionManager();
    }

}
