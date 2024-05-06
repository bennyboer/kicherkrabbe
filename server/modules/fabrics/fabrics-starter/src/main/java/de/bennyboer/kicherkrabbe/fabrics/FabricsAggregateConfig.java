package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.FabricEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class FabricsAggregateConfig {

    @Bean("fabricsEventSourcingRepo")
    public EventSourcingRepo fabricsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("fabrics_events", template, new FabricEventPayloadSerializer());
    }

    @Bean("fabricsEventPublisher")
    public MessagingEventPublisher fabricsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new FabricEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public FabricService fabricService(
            @Qualifier("fabricsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("fabricsEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new FabricService(eventSourcingRepo, eventPublisher);
    }

}
