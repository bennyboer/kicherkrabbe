package de.bennyboer.kicherkrabbe.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.MessagingOutboxEntryPublisher;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.RabbitOutboxEntryPublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.rabbitmq.Sender;

import java.time.Clock;
import java.util.Optional;

@Configuration
@EnableScheduling
@Import(MessagingOutboxTasks.class)
public class MessagingOutboxConfig {

    @Bean
    @ConditionalOnMissingBean(MessagingOutboxRepo.class)
    public MessagingOutboxRepo messagingOutboxRepo(ReactiveMongoTemplate template) {
        return new MongoMessagingOutboxRepo("outbox", template);
    }

    @Bean
    @ConditionalOnMissingBean(MessagingOutboxEntryPublisher.class)
    public MessagingOutboxEntryPublisher messagingOutboxEntryPublisher(
            Sender sender,
            @Qualifier("messagingObjectMapper") ObjectMapper objectMapper
    ) {
        return new RabbitOutboxEntryPublisher(sender, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(MessagingOutbox.class)
    public MessagingOutbox messagingOutbox(
            MessagingOutboxRepo repo,
            MessagingOutboxEntryPublisher publisher,
            Optional<Clock> clock
    ) {
        return new MessagingOutbox(
                repo,
                publisher,
                10,
                clock.orElse(Clock.systemUTC())
        );
    }

}
