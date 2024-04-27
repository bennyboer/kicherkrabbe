package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.users.adapters.messaging.UsersMessaging;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.mongo.MongoUserLookupRepo;
import de.bennyboer.kicherkrabbe.users.internal.UserEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.users.internal.UsersService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
@Import({
        SecurityConfig.class,
        UsersMessaging.class
})
public class UsersModuleConfig {

    @Bean("usersEventSourcingRepo")
    public EventSourcingRepo credentialsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("users_events", template, new UserEventPayloadSerializer());
    }

    @Bean("usersEventPublisher")
    public MessagingEventPublisher credentialsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new UserEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public UsersService usersService(
            @Qualifier("usersEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            MessagingEventPublisher eventPublisher
    ) {
        return new UsersService(eventSourcingRepo, eventPublisher);
    }

    @Bean
    public UserLookupRepo usersLookupRepo(ReactiveMongoTemplate template) {
        return new MongoUserLookupRepo(template);
    }

    @Bean
    public UsersModule usersModule(
            UsersService usersService,
            UserLookupRepo userLookupRepo
    ) {
        return new UsersModule(usersService, userLookupRepo);
    }

}
