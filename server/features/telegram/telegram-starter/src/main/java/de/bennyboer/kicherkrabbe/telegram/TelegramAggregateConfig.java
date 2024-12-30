package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.telegram.persistence.SettingsEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.telegram.settings.SettingsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class TelegramAggregateConfig {

    @Bean("telegramSettingsEventSourcingRepo")
    public EventSourcingRepo telegramSettingsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo(
                "telegram_settings_events",
                template,
                new SettingsEventPayloadSerializer()
        );
    }

    @Bean("telegramSettingsEventPublisher")
    public MessagingEventPublisher telegramSettingsEventPublisher(
            MessagingOutbox outbox,
            Optional<Clock> clock
    ) {
        return new MessagingEventPublisher(
                outbox,
                new SettingsEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("telegramSettingsService")
    public SettingsService settingsService(
            @Qualifier("telegramSettingsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("telegramSettingsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new SettingsService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
