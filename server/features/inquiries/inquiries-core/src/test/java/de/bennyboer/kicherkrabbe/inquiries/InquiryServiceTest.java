package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.inquiries.snapshot.SnapshottedEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

public class InquiryServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final InquiryService inquiryService = new InquiryService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldSendInquiry() {
        var requestId = RequestId.of("REQUEST_ID");
        var sender = Sender.of(
                SenderName.of("John Doe"),
                EMail.of("john.doe+test@kicherkrabbe.com"),
                PhoneNumber.of("+49123456789")
        );
        var subject = Subject.of("Test Inquiry");
        var message = Message.of("Hello, this is a test inquiry.");
        var fingerprint = Fingerprint.of("192.168.1.1");

        // when: sending an inquiry
        var id = send(requestId, sender, subject, message, fingerprint);

        // then: the inquiry is sent
        var inquiry = get(id);
        assertThat(inquiry.getId()).isEqualTo(id);
        assertThat(inquiry.getVersion()).isEqualTo(Version.zero());
        assertThat(inquiry.getSender()).isEqualTo(sender);
        assertThat(inquiry.getSubject()).isEqualTo(subject);
        assertThat(inquiry.getMessage()).isEqualTo(message);
        assertThat(inquiry.getFingerprint()).isEqualTo(fingerprint);
        assertThat(inquiry.isDeleted()).isFalse();
    }

    @Test
    void shouldDeleteInquiry() {
        // given: a sent inquiry
        var requestId = RequestId.of("REQUEST_ID");
        var sender = Sender.of(
                SenderName.of("John Doe"),
                EMail.of("john.doe+test@kicherkrabbe.com"),
                PhoneNumber.of("+49123456789")
        );
        var subject = Subject.of("Test Inquiry");
        var message = Message.of("Hello, this is a test inquiry.");
        var fingerprint = Fingerprint.of("192.168.1.1");

        var id = send(requestId, sender, subject, message, fingerprint);

        // when: deleting the inquiry
        var version = delete(id, Version.zero());

        // then: the inquiry is deleted
        var inquiry = get(id);
        assertThat(inquiry).isNull();

        // and: the inquiries events are collapsed to a single snapshot event
        var events = repo.findEventsByAggregateIdAndType(AggregateId.of(id.getValue()), Inquiry.TYPE, Version.zero())
                .collectList()
                .block();
        assertThat(events).hasSize(1);
        var event = events.getFirst();
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        // and: the snapshot event is anonymized
        var snapshot = (SnapshottedEvent) event.getEvent();
        assertThat(snapshot.getSender().getName()).isEqualTo(SenderName.of("ANONYMIZED"));
        assertThat(snapshot.getSender().getMail()).isEqualTo(EMail.of("anonymized@kicherkrabbe.com"));
        assertThat(snapshot.getSender().getPhone()).isEmpty();
        assertThat(snapshot.getSubject().getValue()).isEqualTo("ANONYMIZED");
        assertThat(snapshot.getMessage().getValue()).isEqualTo("ANONYMIZED");
        assertThat(snapshot.getFingerprint().getIpAddress()).isEmpty();
    }

    private InquiryId send(
            RequestId requestId,
            Sender sender,
            Subject subject,
            Message message,
            Fingerprint fingerprint
    ) {
        return inquiryService.send(requestId, sender, subject, message, fingerprint, Agent.system()).block().getId();
    }

    private Version delete(InquiryId id, Version version) {
        return inquiryService.delete(id, version, Agent.system()).block();
    }

    private Inquiry get(InquiryId id) {
        return inquiryService.get(id).block();
    }

}
