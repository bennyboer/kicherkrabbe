package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.users.adapters.messaging.UsersMessaging;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.mongo.MongoUserLookupRepo;
import de.bennyboer.kicherkrabbe.users.ports.http.UsersHttpConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Clock;
import java.util.Optional;

@Configuration
@Import({
        SecurityConfig.class,
        UsersMessaging.class,
        UsersHttpConfig.class
})
public class UsersModuleConfig {

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

    @Bean
    public UserLookupRepo usersLookupRepo(ReactiveMongoTemplate template) {
        return new MongoUserLookupRepo(template);
    }

    @Bean
    public UsersModule usersModule(
            UsersService usersService,
            UserLookupRepo userLookupRepo,
            ReactiveTransactionManager transactionManager
    ) {
        return new UsersModule(usersService, userLookupRepo, transactionManager);
    }

}
