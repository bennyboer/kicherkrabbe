package de.bennyboer.kicherkrabbe.auth;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsService;
import de.bennyboer.kicherkrabbe.auth.ports.http.AuthHttpConfig;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.mongo.MongoEventSourcingRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;

@Configuration
@Import({
        AuthHttpConfig.class,
        SecurityConfig.class
})
public class AuthModuleConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public EventSourcingRepo credentialsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("credentials_events", template, new CredentialsEventPayloadSerializer());
    }

    @Bean
    public CredentialsService credentialsService(
            @Qualifier("credentialsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            Clock clock
    ) {
        // TODO Replace logging event publisher with a messaging bound one
        return new CredentialsService(eventSourcingRepo, new LoggingEventPublisher(), clock);
    }

    @Bean
    public AuthModule authModule(CredentialsService credentialsService) {
        return new AuthModule(credentialsService);
    }

}
