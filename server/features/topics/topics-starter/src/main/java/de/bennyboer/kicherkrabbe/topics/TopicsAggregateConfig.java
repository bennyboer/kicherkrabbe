package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.topics.persistence.TopicEventPayloadSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class TopicsAggregateConfig {

    @Bean("topicsEventSourcingRepo")
    public EventSourcingRepo topicsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("topics_events", template, new TopicEventPayloadSerializer());
    }

    @Bean("topicsEventPublisher")
    public MessagingEventPublisher topicsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new TopicEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public TopicService topicService(
            @Qualifier("topicsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("topicsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new TopicService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
