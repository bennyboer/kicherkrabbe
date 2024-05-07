package de.bennyboer.kicherkrabbe.messaging.inbox;

import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.mongo.MongoMessagingInboxRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class MessagingInboxConfig {

    @Bean
    @ConditionalOnMissingBean(name = "messagingInboxRepo")
    public MessagingInboxRepo messagingInboxRepo(ReactiveMongoTemplate template) {
        return new MongoMessagingInboxRepo("inbox", template);
    }

    @Bean
    @ConditionalOnMissingBean(MessagingInbox.class)
    public MessagingInbox messagingInbox(
            MessagingInboxRepo repo,
            Optional<Clock> clock
    ) {
        return new MessagingInbox(
                repo,
                clock.orElse(Clock.systemUTC())
        );
    }

}
