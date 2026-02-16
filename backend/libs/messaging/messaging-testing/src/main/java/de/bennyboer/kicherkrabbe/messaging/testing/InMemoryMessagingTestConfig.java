package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory.InMemoryMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.MessagingOutboxEntryPublisher;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Configuration
public class InMemoryMessagingTestConfig {

    @Bean("messagingJsonMapper")
    public JsonMapper messagingJsonMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(NON_NULL))
                .build();
    }

    @Bean
    public InMemoryMessageBus inMemoryMessageBus() {
        return new InMemoryMessageBus();
    }

    @Bean
    public MessageListenerFactory messageListenerFactory(
            ReactiveTransactionManager transactionManager,
            MessagingInbox inbox,
            InMemoryMessageBus messageBus
    ) {
        return new InMemoryMessageListenerFactory(transactionManager, inbox, messageBus);
    }

    @Bean
    public MessagingOutboxEntryPublisher messagingOutboxEntryPublisher(
            InMemoryMessageBus messageBus,
            JsonMapper messagingJsonMapper
    ) {
        return new InMemoryOutboxEntryPublisher(messageBus, messagingJsonMapper);
    }

    @Bean
    public MessagingInboxRepo messagingInboxRepo() {
        return new InMemoryMessagingInboxRepo(false);
    }

    @Bean
    public MessagingOutboxRepo messagingOutboxRepo() {
        return new InMemoryMessagingOutboxRepo();
    }

    @Bean
    public MessagingInbox messagingInbox(MessagingInboxRepo repo) {
        return new MessagingInbox(repo, Clock.systemUTC());
    }

    @Bean
    public MessagingOutbox messagingOutbox(MessagingOutboxRepo repo, MessagingOutboxEntryPublisher publisher) {
        return new MessagingOutbox(repo, publisher, 10, Clock.systemUTC());
    }

    @Bean(destroyMethod = "destroy")
    public InMemoryMessagingOutboxChangeStream messagingOutboxChangeStream(
            MessagingOutboxRepo repo,
            MessagingOutbox outbox
    ) {
        return new InMemoryMessagingOutboxChangeStream(repo, outbox);
    }

    @Bean
    public ReactiveTransactionManager transactionManager() {
        return new MockReactiveTransactionManager();
    }

}
