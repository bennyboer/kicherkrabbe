package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.colors.persistence.ColorEventPayloadSerializer;
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
public class ColorsAggregateConfig {

    @Bean("colorsEventSourcingRepo")
    public EventSourcingRepo colorsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("colors_events", template, new ColorEventPayloadSerializer());
    }

    @Bean("colorsEventPublisher")
    public MessagingEventPublisher colorsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new ColorEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public ColorService colorService(
            @Qualifier("colorsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("colorsEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new ColorService(eventSourcingRepo, eventPublisher);
    }

}
