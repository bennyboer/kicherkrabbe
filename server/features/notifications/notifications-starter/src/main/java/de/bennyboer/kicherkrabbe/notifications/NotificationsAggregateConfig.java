package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.notifications.notification.NotificationService;
import de.bennyboer.kicherkrabbe.notifications.persistence.NotificationEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.notifications.persistence.SettingsEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.notifications.settings.SettingsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class NotificationsAggregateConfig {

    @Bean("notificationsNotificationEventSourcingRepo")
    public EventSourcingRepo notificationsNotificationEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("notifications_events", template, new NotificationEventPayloadSerializer());
    }

    @Bean("notificationsNotificationEventPublisher")
    public MessagingEventPublisher notificationsNotificationEventPublisher(
            MessagingOutbox outbox,
            Optional<Clock> clock
    ) {
        return new MessagingEventPublisher(
                outbox,
                new NotificationEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("notificationsSettingsEventSourcingRepo")
    public EventSourcingRepo notificationsSettingsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo(
                "notifications_settings_events",
                template,
                new SettingsEventPayloadSerializer()
        );
    }

    @Bean("notificationsSettingsEventPublisher")
    public MessagingEventPublisher notificationsSettingsEventPublisher(
            MessagingOutbox outbox,
            Optional<Clock> clock
    ) {
        return new MessagingEventPublisher(
                outbox,
                new SettingsEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean
    public NotificationService notificationService(
            @Qualifier("notificationsNotificationEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("notificationsNotificationEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new NotificationService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

    @Bean
    public SettingsService settingsService(
            @Qualifier("notificationsSettingsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("notificationsSettingsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new SettingsService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
