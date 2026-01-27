package de.bennyboer.kicherkrabbe.mailbox.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MailServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final MailService mailService = new MailService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldReceiveMail() {
        // when: receiving a mail from inquiries
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );

        // then: the mail is received
        var mail = get(id);
        assertThat(mail.getId()).isEqualTo(id);
        assertThat(mail.getVersion()).isEqualTo(Version.zero());
        assertThat(mail.getOrigin()).isEqualTo(Origin.inquiry(OriginId.of("INQUIRY_ID")));
        assertThat(mail.getSender()).isEqualTo(Sender.of(
                SenderName.of("John Doe"),
                EMail.of("john.doe@kicherkrabbe.com"),
                PhoneNumber.of("+49 123 4567 8910")
        ));
        assertThat(mail.getSubject()).isEqualTo(Subject.of("Hello World"));
        assertThat(mail.getContent()).isEqualTo(Content.of("Hello, this is a test mail."));
        assertThat(mail.isRead()).isFalse();
        assertThat(mail.isUnread()).isTrue();
        assertThat(mail.isDeleted()).isFalse();
    }

    @Test
    void shouldMarkMailAsRead() {
        // given: a received mail
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );

        // when: marking the mail as read
        var version = markAsRead(id, Version.zero());

        // then: the mail is marked as read
        var mail = get(id);
        assertThat(mail.isRead()).isTrue();
        assertThat(mail.isUnread()).isFalse();
        assertThat(mail.getVersion()).isEqualTo(version);
    }

    @Test
    void shouldNotMarkMailAsReadGivenAnOutdatedVersion() {
        // given: a received mail
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );
        markAsRead(id, Version.zero());

        // when: marking the mail as read with an outdated version; then: an error is raised
        assertThatThrownBy(() -> markAsRead(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldMarkMailAsUnread() {
        // given: a received mail that is marked as read
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );
        var version = markAsRead(id, Version.zero());

        // when: marking the mail as unread
        version = markAsUnread(id, version);

        // then: the mail is marked as unread
        var mail = get(id);
        assertThat(mail.isRead()).isFalse();
        assertThat(mail.isUnread()).isTrue();
        assertThat(mail.getVersion()).isEqualTo(version);
    }

    @Test
    void shouldNotMarkMailAsUnreadThatIsNotRead() {
        // given: a received mail that is not yet marked as read
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );

        // when: marking the mail as unread; then: an error is raised
        assertThatThrownBy(() -> markAsUnread(id, Version.zero()))
                .matches(e -> e instanceof MailNotReadException);
    }

    @Test
    void shouldNotMarkMailAsReadThatIsAlreadyRead() {
        // given: a received mail that is marked as read
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );
        var version = markAsRead(id, Version.zero());

        // when: marking the mail as read again; then: an error is raised
        assertThatThrownBy(() -> markAsRead(id, version))
                .matches(e -> e instanceof MailNotUnreadException);
    }

    @Test
    void shouldNotMarkMailAsUnreadGivenAnOutdatedVersion() {
        // given: a received mail that is marked as read
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );
        markAsRead(id, Version.zero());

        // when: marking the mail as unread with an outdated version; then: an error is raised
        assertThatThrownBy(() -> markAsUnread(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteMail() {
        // given: a received mail
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );

        // when: deleting the mail
        delete(id, Version.zero());

        // then: the mail is deleted
        var mail = get(id);
        assertThat(mail).isNull();

        // and: there is only a single snapshot event in the repository since it is collapsed
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Mail.TYPE,
                Version.zero()
        ).collectList().block();
        assertThat(events).hasSize(1);
        var event = events.get(0);
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        // and: the snapshot event is anonymized
        var snapshot = (SnapshotEvent) event.getEvent();
        var state = snapshot.getState();
        @SuppressWarnings("unchecked")
        var senderMap = (Map<String, Object>) state.get("sender");
        assertThat(senderMap.get("name")).isEqualTo("ANONYMIZED");
        assertThat(senderMap.get("mail")).isEqualTo("anonymized@kicherkrabbe.com");
        assertThat(senderMap.get("phone")).isNull();
        assertThat(state.get("subject")).isEqualTo("ANONYMIZED");
        assertThat(state.get("content")).isEqualTo("ANONYMIZED");

        // and: there can be no more events for the mail
        assertThatThrownBy(() -> markAsRead(id, event.getMetadata().getAggregateVersion()))
                .matches(e -> e instanceof IllegalArgumentException && e.getMessage()
                        .equals("Cannot apply command to deleted aggregate"));
    }

    @Test
    void shouldNotDeleteMailGivenAnOutdatedVersion() {
        // given: a received mail
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );
        markAsRead(id, Version.zero());

        // when: deleting the mail with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a received mail
        var id = receive(
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49 123 4567 8910")
                ),
                Subject.of("Hello World"),
                Content.of("Hello, this is a test mail.")
        );

        // when: marking the mail as read and unread 200 times
        var version = Version.zero();
        for (int i = 0; i < 100; i++) {
            version = markAsRead(id, version);
            version = markAsUnread(id, version);
        }

        // then: the mail is marked as unread
        var mail = get(id);
        assertThat(mail.getVersion()).isEqualTo(Version.of(202));
        assertThat(mail.isRead()).isFalse();
        assertThat(mail.isUnread()).isTrue();

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Mail.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private MailId receive(Origin origin, Sender sender, Subject subject, Content content) {
        return mailService.receive(origin, sender, subject, content, Agent.system()).block().getId();
    }

    private Mail get(MailId id) {
        return mailService.get(id).block();
    }

    private Version markAsRead(MailId id, Version version) {
        return mailService.markAsRead(id, version, Agent.system()).block();
    }

    private Version markAsUnread(MailId id, Version version) {
        return mailService.markAsUnread(id, version, Agent.system()).block();
    }

    private Version delete(MailId id, Version version) {
        return mailService.delete(id, version, Agent.system()).block();
    }

}
