package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.mailing.mail.*;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
                Sender.of(EMail.of("john.doe@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail"),
                Text.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
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
                Sender.of(EMail.of("john.doe@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail"),
                Text.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var mail2 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Sender.of(EMail.of("no-reply@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail 2"),
                Text.of("Hello, this is another test mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
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
                Sender.of(EMail.of("john.doe@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail"),
                Text.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var mail2 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Sender.of(EMail.of("no-reply@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail 2"),
                Text.of("Hello, this is another test mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        update(mail1);
        update(mail2);

        // when: querying mails
        var page = query(0, 10);

        // then: all mails are found in the correct order
        assertThat(page.getMails()).containsExactly(mail2, mail1);

        // when: querying mails with paging
        page = query(1, 1);

        // then: only the first mail is found
        assertThat(page.getMails()).containsExactly(mail1);

        // when: querying another mails page
        page = query(10, 1);

        // then: no mails are found
        assertThat(page.getMails()).isEmpty();
    }

    @Test
    void shouldCountMailsAfterASpecificDate() {
        // given: some mails
        var mail1 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Sender.of(EMail.of("john.doe@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail"),
                Text.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var mail2 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Sender.of(EMail.of("no-reply@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail 2"),
                Text.of("Hello, this is another test mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        update(mail1);
        update(mail2);

        // when: counting mails after a specific date
        var count = countAfter(Instant.parse("2024-03-12T12:40:00.00Z"));

        // then: only the second mail is found
        assertThat(count).isEqualTo(1);

        // when: counting mails after a date in the future
        count = countAfter(Instant.parse("2024-03-12T12:50:00.00Z"));

        // then: no mails are found
        assertThat(count).isEqualTo(0);

        // when: counting mails after a date in the past
        count = countAfter(Instant.parse("2024-03-12T12:20:00.00Z"));

        // then: all mails are found
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindMailsOlderThanASpecificDate() {
        // given: some mails
        var mail1 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Sender.of(EMail.of("john.doe@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail"),
                Text.of("Hello, this is a test mail."),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var mail2 = LookupMail.of(
                MailId.create(),
                Version.zero(),
                Sender.of(EMail.of("no-reply@kicherkrabbe.com")),
                Set.of(Receiver.of(EMail.of("jane.doe@kicherkrabbe.com"))),
                Subject.of("Test Mail 2"),
                Text.of("Hello, this is another test mail."),
                Instant.parse("2024-03-12T12:45:00.00Z")
        );
        update(mail1);
        update(mail2);

        // when: finding mails older than a specific date
        var mails = findOlderThan(Instant.parse("2024-03-12T12:40:00.00Z"));

        // then: only the first mail is found
        assertThat(mails).containsExactly(mail1);

        // when: finding mails older than a date in the future
        mails = findOlderThan(Instant.parse("2024-03-12T12:50:00.00Z"));

        // then: all mails are found
        assertThat(mails).containsExactlyInAnyOrder(mail1, mail2);

        // when: finding mails older than a date in the past
        mails = findOlderThan(Instant.parse("2024-03-12T12:20:00.00Z"));

        // then: no mails are found
        assertThat(mails).isEmpty();
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

    private LookupMailPage query(long skip, long limit) {
        return repo.query(skip, limit).block();
    }

    private long countAfter(Instant instant) {
        return repo.countAfter(instant).block();
    }

    private List<LookupMail> findOlderThan(Instant instant) {
        return repo.findOlderThan(instant).collectList().block();
    }

}
