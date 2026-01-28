package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.messaging.MessagingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo.MongoEventSourcingRepo;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailService;
import de.bennyboer.kicherkrabbe.mailbox.persistence.MailEventPayloadSerializer;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class MailboxAggregateConfig {

    @Bean("mailboxMailEventSourcingRepo")
    public EventSourcingRepo mailboxMailEventSourcingRepo(ReactiveMongoTemplate template) {
        return new MongoEventSourcingRepo("mailbox_mails_events", template, new MailEventPayloadSerializer());
    }

    @Bean("mailboxMailEventPublisher")
    public MessagingEventPublisher mailboxMailEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingEventPublisher(
                outbox,
                new MailEventPayloadSerializer(),
                clock.orElse(Clock.systemUTC())
        );
    }

    @Bean("mailboxMailService")
    public MailService mailService(
            @Qualifier("mailboxMailEventSourcingRepo") EventSourcingRepo eventSourcingRepo,
            @Qualifier("mailboxMailEventPublisher") MessagingEventPublisher eventPublisher,
            Optional<Clock> clock
    ) {
        return new MailService(eventSourcingRepo, eventPublisher, clock.orElse(Clock.systemUTC()));
    }

}
