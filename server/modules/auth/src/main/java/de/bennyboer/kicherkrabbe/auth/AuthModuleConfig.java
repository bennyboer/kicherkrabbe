package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.adapters.messaging.CredentialsCreatedListener;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsService;
import de.bennyboer.kicherkrabbe.auth.ports.http.AuthHttpConfig;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
@Import({
        AuthHttpConfig.class,
        SecurityConfig.class
})
public class AuthModuleConfig {

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
            MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new CredentialsService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

    @Bean
    public AuthModule authModule(CredentialsService credentialsService) {
        return new AuthModule(credentialsService);
    }

    @Bean
    public CredentialsCreatedListener credentialsCreatedListener(MessageListener messageListener) {
        return new CredentialsCreatedListener(messageListener);
    }

}
