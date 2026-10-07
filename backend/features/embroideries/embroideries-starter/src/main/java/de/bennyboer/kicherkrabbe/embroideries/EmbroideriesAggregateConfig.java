package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.persistence.EmbroideryEventPayloadSerializer;
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
public class EmbroideriesAggregateConfig {

    @Bean("embroideriesEventSourcingRepo")
    public EventSourcingRepo embroideriesEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("embroideries_events", template, new EmbroideryEventPayloadSerializer());
    }

    @Bean("embroideriesEventPublisher")
    public MessagingEventPublisher embroideriesEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new EmbroideryEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public EmbroideryService embroideryService(
            @Qualifier("embroideriesEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("embroideriesEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new EmbroideryService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
