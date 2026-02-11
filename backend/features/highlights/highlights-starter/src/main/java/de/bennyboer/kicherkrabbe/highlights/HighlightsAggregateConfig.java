package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.HighlightEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class HighlightsAggregateConfig {

    @Bean("highlightsEventSourcingRepo")
    public EventSourcingRepo highlightsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("highlights_events", template, new HighlightEventPayloadSerializer());
    }

    @Bean("highlightsEventPublisher")
    public MessagingEventPublisher highlightsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new HighlightEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public HighlightService highlightService(
            @Qualifier("highlightsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("highlightsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new HighlightService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
