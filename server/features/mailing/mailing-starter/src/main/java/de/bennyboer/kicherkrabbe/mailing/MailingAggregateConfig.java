package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.mailing.mail.MailService;
import de.bennyboer.kicherkrabbe.mailing.persistence.MailEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.mailing.persistence.SettingsEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.mailing.settings.SettingsService;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class MailingAggregateConfig {

    @Bean("mailingSettingsEventSourcingRepo")
    public EventSourcingRepo mailingSettingsEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo(
                "mailing_settings_events",
                template,
                new SettingsEventPayloadSerializer()
        );
    }

    @Bean("mailingSettingsEventPublisher")
    public MessagingEventPublisher mailingSettingsEventPublisher(
            MessagingOutbox outbox,
            Optional<Clock> clock
    ) {
        return new MessagingEventPublisher(
                outbox,
                new SettingsEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("mailingMailEventSourcingRepo")
    public EventSourcingRepo mailingMailEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo(
                "mailing_mails_events",
                template,
                new MailEventPayloadSerializer()
        );
    }

    @Bean("mailingMailEventPublisher")
    public MessagingEventPublisher mailingMailEventPublisher(
            MessagingOutbox outbox,
            Optional<Clock> clock
    ) {
        return new MessagingEventPublisher(
                outbox,
                new MailEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("mailingSettingsService")
    public SettingsService settingsService(
            @Qualifier("mailingSettingsEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("mailingSettingsEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new SettingsService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

    @Bean("mailingMailService")
    public MailService mailService(
            @Qualifier("mailingMailEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("mailingMailEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new MailService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
