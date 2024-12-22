package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.mailbox.mail.*;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.mailbox.mail.Status.READ;
import static de.bennyboer.kicherkrabbe.mailbox.mail.Status.UNREAD;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class MailLookupRepoTest {

    private MailLookupRepo repo;

    protected abstract MailLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateMail() {
        // given: a mail to update
        var mail = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Origin.inquiry(OriginId.of("INQUIRY_ID")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456789")
                ),
                Subject.of("Test Mail"),
                Content.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z"),
                READ,
                Instant.parse("2024-03-12T12:45:00.00Z")
        );

        // when: updating the mail
        update(mail);

        // then: the mail is updated
        var actualMail = findById(mail.getId());
        assertThat(actualMail).isEqualTo(mail);
    }

    @Test
    void shouldRemoveMail() {
        // given: some mails
        var mail1 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Origin.inquiry(OriginId.of("INQUIRY_ID_1")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456789")
                ),
                Subject.of("Test Mail"),
                Content.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z"),
                READ,
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        var mail2 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Origin.inquiry(OriginId.of("INQUIRY_ID_2")),
                Sender.of(
                        SenderName.of("Jane Doe"),
                        EMail.of("jane.doe@kicherkrabbe.com"),
                        null
                ),
                Subject.of("Test Mail"),
                Content.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z"),
                UNREAD,
                null
        );
        update(mail1);
        update(mail2);

        // when: removing a mail
        remove(mail1.getId());

        // then: the mail is removed
        var actualMail = findById(mail1.getId());
        assertThat(actualMail).isNull();

        // and: the other mail is still there
        actualMail = findById(mail2.getId());
        assertThat(actualMail).isEqualTo(mail2);
    }

    @Test
    void shouldQueryMails() {
        // given: some mails
        var mail1 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Origin.inquiry(OriginId.of("INQUIRY_ID_1")),
                Sender.of(
                        SenderName.of("John Doe"),
                        EMail.of("john.doe@kicherkrabbe.com"),
                        PhoneNumber.of("+49123456789")
                ),
                Subject.of("Test Mail 1"),
                Content.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z"),
                READ,
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        var mail2 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Origin.inquiry(OriginId.of("INQUIRY_ID_2")),
                Sender.of(
                        SenderName.of("Jane Doe"),
                        EMail.of("jane.doe@kicherkrabbe.com"),
                        null
                ),
                Subject.of("Test Mail 2"),
                Content.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T13:15:00.00Z"),
                UNREAD,
                null
        );
        update(mail1);
        update(mail2);

        // when: querying mails by search term
        var page = query("Doe", null, 0, 10);

        // then: the mails are found ordered by received date
        assertThat(page.getMails()).containsExactly(mail2, mail1);

        // when: querying mails by another search term
        page = query("1", null, 0, 10);

        // then: only the first mail is found
        assertThat(page.getMails()).containsExactly(mail1);

        // when: querying mails by another search term
        page = query("jane.doe", null, 0, 10);

        // then: only the second mail is found
        assertThat(page.getMails()).containsExactly(mail2);

        // when: querying mails by status
        page = query("", UNREAD, 0, 10);

        // then: only the second mail is found
        assertThat(page.getMails()).containsExactly(mail2);

        // when: querying mails by another status
        page = query("", READ, 0, 10);

        // then: only the first mail is found
        assertThat(page.getMails()).containsExactly(mail1);

        // when: querying mails by status and search term
        page = query("Doe", UNREAD, 0, 10);

        // then: only the second mail is found
        assertThat(page.getMails()).containsExactly(mail2);

        // when: querying mails with paging
        page = query("", null, 1, 1);

        // then: only the first mail is found
        assertThat(page.getMails()).containsExactly(mail1);
    }

    private void update(LookupMail mail) {
        repo.update(mail).block();
    }

    private void remove(MailId mailId) {
        repo.remove(mailId).block();
    }

    private LookupMail findById(MailId id) {
        return repo.findById(id).block();
    }

    private LookupMailPage query(String searchTerm, @Nullable Status status, long skip, long limit) {
        return repo.query(searchTerm, status, skip, limit).block();
    }

}
