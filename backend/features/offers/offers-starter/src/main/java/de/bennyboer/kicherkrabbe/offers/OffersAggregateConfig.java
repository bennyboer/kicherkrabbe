package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.offers.persistence.OfferEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class OffersAggregateConfig {

    @Bean("offersEventSourcingRepo")
    public EventSourcingRepo offersEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("offers_events", template, new OfferEventPayloadSerializer());
    }

    @Bean("offersEventPublisher")
    public MessagingEventPublisher offersEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new OfferEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public OfferService offerService(
            @Qualifier("offersEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("offersEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new OfferService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
