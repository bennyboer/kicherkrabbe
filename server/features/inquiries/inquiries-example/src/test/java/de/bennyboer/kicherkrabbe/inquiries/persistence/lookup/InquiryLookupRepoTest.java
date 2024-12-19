package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.inquiries.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class InquiryLookupRepoTest {

    private InquiryLookupRepo repo;

    protected abstract InquiryLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateInquiry() {
        // given: an inquiry to update
        var inquiry = LookupInquiry.of(
                InquiryId.create(),
                Version.zero(),
                RequestId.of("REQUEST_ID"),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456789")
                ),
                Subject.of("Test Inquiry"),
                Message.of("Hello, this is a test inquiry."),
                Fingerprint.of("192.168.1.1"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the inquiry
        update(inquiry);

        // then: the inquiry is updated
        var actualInquiry = find(inquiry.getId());
        assertThat(actualInquiry).isEqualTo(inquiry);
    }

    @Test
    void shouldRemoveInquiry() {
        // given: some inquiries
        var inquiry1 = LookupInquiry.of(
                InquiryId.create(),
                Version.zero(),
                RequestId.of("REQUEST_ID_1"),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456789")
                ),
                Subject.of("Test Inquiry"),
                Message.of("Hello, this is a test inquiry."),
                Fingerprint.of("192.168.1.1"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var inquiry2 = LookupInquiry.of(
                InquiryId.create(),
                Version.zero(),
                RequestId.of("REQUEST_ID_2"),
                Sender.of(
                        SenderName.of("Jane Doe"),
                        EMail.of("jane.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456782")
                ),
                Subject.of("Test Inquiry"),
                Message.of("Hello, this is a test inquiry."),
                Fingerprint.of("192.168.1.2"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(inquiry1);
        update(inquiry2);

        // when: removing an inquiry
        remove(inquiry1.getId());

        // then: the inquiry is removed
        var actualInquiry = find(inquiry1.getId());
        assertThat(actualInquiry).isNull();

        // and: the other inquiry is still there
        actualInquiry = find(inquiry2.getId());
        assertThat(actualInquiry).isEqualTo(inquiry2);
    }

    @Test
    void shouldFindByRequestId() {
        // given: some inquiries
        // given: some inquiries
        var inquiry1 = LookupInquiry.of(
                InquiryId.create(),
                Version.zero(),
                RequestId.of("REQUEST_ID_1"),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456789")
                ),
                Subject.of("Test Inquiry"),
                Message.of("Hello, this is a test inquiry."),
                Fingerprint.of("192.168.1.1"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var inquiry2 = LookupInquiry.of(
                InquiryId.create(),
                Version.zero(),
                RequestId.of("REQUEST_ID_2"),
                Sender.of(
                        SenderName.of("Jane Doe"),
                        EMail.of("jane.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456782")
                ),
                Subject.of("Test Inquiry"),
                Message.of("Hello, this is a test inquiry."),
                Fingerprint.of("192.168.1.2"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(inquiry1);
        update(inquiry2);

        // when: finding an inquiry by request id
        var actualInquiry = repo.findByRequestId(inquiry1.getRequestId()).block();

        // then: the inquiry is found
        assertThat(actualInquiry).isEqualTo(inquiry1);

        // when: finding an inquiry by request id
        actualInquiry = repo.findByRequestId(inquiry2.getRequestId()).block();

        // then: the inquiry is found
        assertThat(actualInquiry).isEqualTo(inquiry2);
    }

    private void update(LookupInquiry inquiry) {
        repo.update(inquiry).block();
    }

    private void remove(InquiryId inquiryId) {
        repo.remove(inquiryId).block();
    }

    private LookupInquiry find(InquiryId id) {
        return repo.find(id).block();
    }

}
