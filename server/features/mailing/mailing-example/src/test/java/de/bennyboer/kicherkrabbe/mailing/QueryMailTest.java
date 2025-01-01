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

public class QueryMailTest extends MailingModuleTest {

    @Test
    void shouldQueryMail() {
        // given: the system user is allowed to send mails
        allowSystemUserToSendMails();

        // and: the current user is allowed to read and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: a sent mail
        setTime(Instant.parse("2024-12-08T10:15:30.000Z"));
        var request = new SendMailRequest();
        request.sender = new SenderDTO();
        request.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request.receivers = Set.of(receiver1);
        request.subject = "Hello";
        request.text = "Hello John";
        var mailId = sendMail(request, Agent.system()).id;

        // when: querying the mail
        var mail = getMail(mailId, Agent.user(AgentId.of("USER_ID"))).mail;

        // then: the response contains the expected mail
        assertThat(mail.sender.mail).isEqualTo("no-reply@kicherkrabbe.com");
        assertThat(mail.receivers).hasSize(1);
        assertThat(mail.receivers.stream().findFirst().orElseThrow().mail).isEqualTo("john.doe@kicherkrabbe.com");
        assertThat(mail.subject).isEqualTo("Hello");
        assertThat(mail.text).isEqualTo("Hello John");
        assertThat(mail.sentAt).isEqualTo(Instant.parse("2024-12-08T10:15:30.000Z"));
    }

    @Test
    void shouldNotQueryMailWhenUserDoesNotHavePermission() {
        // given: the user does not have permission to read mails

        // when: the user queries the mails; then: an exception is thrown
        assertThatThrownBy(() -> getMail("MAIL_ID", Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
