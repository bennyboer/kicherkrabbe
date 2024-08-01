package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.categories.persistence.CategoryEventPayloadSerializer;
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
public class CategoriesAggregateConfig {

    @Bean("categoriesEventSourcingRepo")
    public EventSourcingRepo categoriesEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("categories_events", template, new CategoryEventPayloadSerializer());
    }

    @Bean("categoriesEventPublisher")
    public MessagingEventPublisher categoriesEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new CategoryEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public CategoryService categoryService(
            @Qualifier("categoriesEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("categoriesEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new CategoryService(eventSourcingRepo, eventPublisher);
    }

}
