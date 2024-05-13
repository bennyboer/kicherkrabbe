package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.users.persistence.UserEventPayloadSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class UsersAggregateConfig {

    @Bean("usersEventSourcingRepo")
    public EventSourcingRepo credentialsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("users_events", template, new UserEventPayloadSerializer());
    }

    @Bean("usersEventPublisher")
    public MessagingEventPublisher usersEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new UserEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public UsersService usersService(
            @Qualifier("usersEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("usersEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new UsersService(eventSourcingRepo, eventPublisher);
    }

}
