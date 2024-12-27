package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.InquiryEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.inquiries.persistence.SettingsEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.inquiries.settings.SettingsService;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class InquiriesAggregateConfig {

    @Bean("inquiriesEventSourcingRepo")
    public EventSourcingRepo inquiriesEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("inquiries_events", template, new InquiryEventPayloadSerializer());
    }

    @Bean("inquiriesEventPublisher")
    public MessagingEventPublisher inquiriesEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new InquiryEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("inquiriesSettingsEventSourcingRepo")
    public EventSourcingRepo inquiriesSettingsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("inquiries_settings_events", template, new SettingsEventPayloadSerializer());
    }

    @Bean("inquiriesSettingsEventPublisher")
    public MessagingEventPublisher inquiriesSettingsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new SettingsEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public InquiryService inquiryService(
            @Qualifier("inquiriesEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("inquiriesEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new InquiryService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

    @Bean("inquiriesSettingsService")
    public SettingsService settingsService(
            @Qualifier("inquiriesSettingsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("inquiriesSettingsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new SettingsService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
