package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.persistence.AssetEventPayloadSerializer;
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
public class AssetsAggregateConfig {

    @Bean("assetsEventSourcingRepo")
    public EventSourcingRepo assetsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("assets_events", template, new AssetEventPayloadSerializer());
    }

    @Bean("assetsEventPublisher")
    public MessagingEventPublisher assetsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new AssetEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public AssetService assetService(
            @Qualifier("assetsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("assetsEventPublisher") MessagingEventPublisher eventPublisher
    ) {
        return new AssetService(eventSourcingRepo, eventPublisher);
    }

}
