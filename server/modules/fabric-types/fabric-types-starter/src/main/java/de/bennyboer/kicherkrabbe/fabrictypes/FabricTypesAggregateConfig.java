package de.bennyboer.kicherkrabbe.fabrictypes;

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
public class FabricTypesAggregateConfig {

    @Bean("fabricTypesEventSourcingRepo")
    public EventSourcingRepo fabricTypesEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("fabric_types_events", template, new FabricTypeEventPayloadSerializer());
    }

    @Bean("fabricTypesEventPublisher")
    public MessagingEventPublisher fabricTypesEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new FabricTypeEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public FabricTypeService fabricTypeService(
            @Qualifier("fabricTypesEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("fabricTypesEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new FabricTypeService(eventSourcingRepo, eventPublisher);
    }

}
