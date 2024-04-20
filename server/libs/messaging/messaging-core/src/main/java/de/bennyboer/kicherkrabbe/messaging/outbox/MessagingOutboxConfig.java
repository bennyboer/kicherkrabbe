package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.mongo.MongoMessagingOutboxRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Optional;

@Configuration
@EnableScheduling
public class MessagingOutboxConfig {

    @Bean
    public MessagingOutboxRepo messagingOutboxRepo(ReactiveMongoTemplate template) {
        return new MongoMessagingOutboxRepo("outbox", template);
    }

    @Bean
    public MessagingOutbox messagingOutbox(MessagingOutboxRepo repo, Optional<Clock> clock) {
        return new MessagingOutbox(
                repo,
                entries -> {
                    System.out.printf("Publishing %d entries%n", entries.size());
                    return Mono.empty();
                },
                10,
                clock.orElse(Clock.systemUTC())
        );
    }

}
