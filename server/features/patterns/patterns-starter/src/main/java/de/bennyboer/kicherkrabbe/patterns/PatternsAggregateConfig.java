package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.patterns.persistence.PatternEventPayloadSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class PatternsAggregateConfig {

    @Bean("patternsEventSourcingRepo")
    public EventSourcingRepo patternsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("patterns_events", template, new PatternEventPayloadSerializer());
    }

    @Bean("patternsEventPublisher")
    public MessagingEventPublisher patternsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new PatternEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public PatternService patternService(
            @Qualifier("patternsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("patternsEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new PatternService(eventSourcingRepo, eventPublisher);
    }

}
