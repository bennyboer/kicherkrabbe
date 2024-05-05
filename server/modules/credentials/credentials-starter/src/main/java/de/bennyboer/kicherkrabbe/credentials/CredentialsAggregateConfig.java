package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class CredentialsAggregateConfig {

    @Bean("credentialsEventSourcingRepo")
    public EventSourcingRepo credentialsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("credentials_events", template, new CredentialsEventPayloadSerializer());
    }

    @Bean("credentialsEventPublisher")
    public MessagingEventPublisher credentialsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new CredentialsEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public CredentialsService credentialsService(
            @Qualifier("credentialsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("credentialsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new CredentialsService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
