package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.MessagingOutboxEntryPublisher;
import de.bennyboer.kicherkrabbe.messaging.outbox.publisher.RabbitOutboxEntryPublisher;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.json.JsonMapper;

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
            RabbitTemplate rabbitTemplate,
            RabbitAdmin rabbitAdmin,
            @Qualifier("messagingJsonMapper") JsonMapper jsonMapper
    ) {
        return new RabbitOutboxEntryPublisher(rabbitTemplate, rabbitAdmin, jsonMapper);
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

    @Bean(destroyMethod = "destroy")
    public MessagingOutboxChangeStream messagingOutboxChangeStream(
            MessagingOutboxRepo repo,
            MessagingOutbox outbox
    ) {
        return new MessagingOutboxChangeStream(repo, outbox);
    }

}
