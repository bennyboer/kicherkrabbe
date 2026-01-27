package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryMailsTest extends MailingModuleTest {

    @Test
    void shouldQueryMails() {
        // given: the system user is allowed to send mails
        allowSystemUserToSendMails();

        // and: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: some sent mails at different times
        setTime(Instant.parse("2024-12-08T10:15:30.000Z"));
        var request1 = new SendMailRequest();
        request1.sender = new SenderDTO();
        request1.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request1.receivers = Set.of(receiver1);
        request1.subject = "Hello";
        request1.text = "Hello John";
        sendMail(request1, Agent.system());

        setTime(Instant.parse("2024-12-09T12:00:00.000Z"));
        var request2 = new SendMailRequest();
        request2.sender = new SenderDTO();
        request2.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver2 = new ReceiverDTO();
        receiver2.mail = "jane.doe@kicherkrabbe.com";
        request2.receivers = Set.of(receiver2);
        request2.subject = "Hello";
        request2.text = "Hello Jane";
        sendMail(request2, Agent.system());

        // when: querying the mails
        var mails = getMails(0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the expected mails ordered in descending order by sentAt
        assertThat(mails.total).isEqualTo(2);
        assertThat(mails.mails).hasSize(2);

        var mail1 = mails.mails.get(0);
        assertThat(mail1.sender.mail).isEqualTo("no-reply@kicherkrabbe.com");
        assertThat(mail1.receivers).hasSize(1);
        assertThat(mail1.receivers.stream().findFirst().orElseThrow().mail).isEqualTo("jane.doe@kicherkrabbe.com");
        assertThat(mail1.subject).isEqualTo("Hello");
        assertThat(mail1.text).isEqualTo("Hello Jane");
        assertThat(mail1.sentAt).isEqualTo(Instant.parse("2024-12-09T12:00:00.000Z"));

        var mail2 = mails.mails.get(1);
        assertThat(mail2.sender.mail).isEqualTo("no-reply@kicherkrabbe.com");
        assertThat(mail2.receivers).hasSize(1);
        assertThat(mail2.receivers.stream().findFirst().orElseThrow().mail).isEqualTo("john.doe@kicherkrabbe.com");
        assertThat(mail2.subject).isEqualTo("Hello");
        assertThat(mail2.text).isEqualTo("Hello John");
        assertThat(mail2.sentAt).isEqualTo(Instant.parse("2024-12-08T10:15:30.000Z"));

        // when: querying the mails with paging
        var mailsPage1 = getMails(0, 1, Agent.user(AgentId.of("USER_ID")));

        // then: the response contains the expected mails
        assertThat(mailsPage1.total).isEqualTo(2);
        assertThat(mailsPage1.mails).hasSize(1);
        assertThat(mailsPage1.mails.get(0)).isEqualTo(mail1);
    }

    @Test
    void shouldNotQueryMailsWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to read mails

        // when: the user queries the mails; then: an exception is thrown
        assertThatThrownBy(() -> getMails(0, 10, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldReturnEmptyListIfNoMailIsReceivedYet() {
        // given: the user is allowed to read mails and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // when: querying the mails
        var mails = getMails(0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the response contains no mails
        assertThat(mails.total).isEqualTo(0);
        assertThat(mails.mails).isEmpty();
    }

}
