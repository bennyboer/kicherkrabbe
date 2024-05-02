package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.auth.tokens.TokenGenerator;
import de.bennyboer.kicherkrabbe.credentials.adapters.messaging.CredentialsMessaging;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.mongo.MongoCredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.ports.http.CredentialsHttpConfig;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Optional;

@Configuration
@Import({
        CredentialsHttpConfig.class,
        SecurityConfig.class,
        CredentialsMessaging.class
})
public class CredentialsModuleConfig {

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

    @Bean
    public CredentialsLookupRepo credentialsLookupRepo(ReactiveMongoTemplate template) {
        return new MongoCredentialsLookupRepo(template);
    }

    @Bean("credentialsPermissionsRepo")
    public PermissionsRepo credentialsPermissionsRepo(ReactiveMongoTemplate template) {
        return new MongoPermissionsRepo("credentials_permissions", template);
    }

    @Bean("credentialsPermissionsService")
    public PermissionsService credentialsPermissionsService(
            @Qualifier("credentialsPermissionsRepo") PermissionsRepo permissionsRepo
    ) {
        return new PermissionsService(permissionsRepo, event -> Mono.empty());
    }

    @Bean
    public CredentialsModule credentialsModule(
            CredentialsService credentialsService,
            CredentialsLookupRepo credentialsLookupRepo,
            @Qualifier("credentialsPermissionsService") PermissionsService permissionsService,
            TokenGenerator tokenGenerator
    ) {
        return new CredentialsModule(credentialsService, credentialsLookupRepo, permissionsService, tokenGenerator);
    }

}
