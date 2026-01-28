package de.bennyboer.kicherkrabbe.mailing.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.mailing.MailingService;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Map;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.mailing.MailingService.MAILGUN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MailServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final MailService mailService = new MailService(
            repo,
            eventPublisher,
            Clock.systemUTC()
    );

    @Test
    void shouldSendMail() {
        // when: sending a mail
        var sender = Sender.of(EMail.of("no-reply@kicherkrabbe.com"));
        var receivers = Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com")));
        var subject = Subject.of("Hello, Jane!");
        var text = Text.of("Hello, Jane! How are you?");
        var mailingService = MAILGUN;
        var mailId = send(
                sender,
                receivers,
                subject,
                text,
                mailingService
        );

        // then: the mail is sent
        var mail = get(mailId);
        assertThat(mail.getId()).isEqualTo(mailId);
        assertThat(mail.getVersion()).isEqualTo(Version.zero());
        assertThat(mail.getSender()).isEqualTo(sender);
        assertThat(mail.getReceivers()).isEqualTo(receivers);
        assertThat(mail.getSubject()).isEqualTo(subject);
        assertThat(mail.getText()).isEqualTo(text);
        assertThat(mail.getMailingService()).isEqualTo(mailingService);
        assertThat(mail.getSentAt()).isNotNull();
        assertThat(mail.getDeletedAt()).isEmpty();
    }

    @Test
    void shouldDeleteMail() {
        // given: a sent mail
        var sender = Sender.of(EMail.of("no-reply@kicherkrabbe.com"));
        var receivers = Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com")));
        var subject = Subject.of("Hello, Jane!");
        var text = Text.of("Hello, Jane! How are you?");
        var mailingService = MAILGUN;
        var mailId = send(
                sender,
                receivers,
                subject,
                text,
                mailingService
        );

        // when: deleting the mail
        var version = delete(mailId, Version.zero());

        // then: the mail is deleted
        var mail = get(mailId);
        assertThat(mail).isNull();

        // and: there is a single snapshot event in the database with anonymized details
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(mailId.getValue()),
                Mail.TYPE,
                Version.zero()
        ).collectList().block();
        assertThat(events).hasSize(1);
        var event = events.get(0);
        assertThat(event.getMetadata().getAggregateVersion()).isEqualTo(version);
        assertThat(event.getMetadata().getAggregateId()).isEqualTo(AggregateId.of(mailId.getValue()));
        assertThat(event.getMetadata().getAgent()).isEqualTo(Agent.system());
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        var snapshot = (SnapshotEvent) event.getEvent();
        var state = snapshot.getState();
        @SuppressWarnings("unchecked")
        var senderMap = (Map<String, Object>) state.get("sender");
        assertThat(senderMap.get("mail")).isEqualTo("anonymized@kicherkrabbe.com");
        assertThat(state.get("subject")).isEqualTo("ANONYMIZED");
        assertThat(state.get("text")).isEqualTo("ANONYMIZED");
        assertThat(state.get("mailingService")).isEqualTo("MAILGUN");
        assertThat(state.get("sentAt")).isNotNull();
        assertThat(state.get("deletedAt")).isNotNull();
    }

    @Test
    void shouldRaiseErrorWhenTryingToDeleteWithOutdatedVersion() {
        // given: a sent mail
        var sender = Sender.of(EMail.of("no-reply@kicherkrabbe.com"));
        var receivers = Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com")));
        var subject = Subject.of("Hello, Jane!");
        var text = Text.of("Hello, Jane! How are you?");
        var mailingService = MAILGUN;
        var mailId = send(
                sender,
                receivers,
                subject,
                text,
                mailingService
        );

        // and: the mail is deleted
        var version = delete(mailId, Version.zero());

        // when: trying to delete the mail with outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(mailId, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot apply command to deleted aggregate");
    }

    private MailId send(
            Sender sender,
            Set<Receiver> receivers,
            Subject subject,
            Text text,
            MailingService mailingService
    ) {
        return mailService.send(
                sender,
                receivers,
                subject,
                text,
                mailingService,
                Agent.system()
        ).block().getId();
    }

    private Mail get(MailId id) {
        return mailService.get(id).block();
    }

    private Version delete(MailId id, Version version) {
        return mailService.delete(
                id,
                version,
                Agent.system()
        ).block();
    }

}
