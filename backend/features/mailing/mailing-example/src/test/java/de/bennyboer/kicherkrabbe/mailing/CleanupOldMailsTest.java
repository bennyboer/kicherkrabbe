package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupOldMailsTest extends MailingModuleTest {

    @Test
    void shouldCleanupOldMails() {
        // given: the system user is allowed to send mails
        allowSystemUserToSendMails();

        // and: the current user is allowed to read mails and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: some sent mails at different times
        setTime(Instant.parse("2024-02-08T10:15:30.000Z"));
        var request1 = new SendMailRequest();
        request1.sender = new SenderDTO();
        request1.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request1.receivers = Set.of(receiver1);
        request1.subject = "Hello";
        request1.text = "Hello John";
        sendMail(request1, Agent.system());

        setTime(Instant.parse("2024-03-02T12:00:00.000Z"));
        var request2 = new SendMailRequest();
        request2.sender = new SenderDTO();
        request2.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver2 = new ReceiverDTO();
        receiver2.mail = "jane.doe@kicherkrabbe.com";
        request2.receivers = Set.of(receiver2);
        request2.subject = "Hello";
        request2.text = "Hello Jane";
        sendMail(request2, Agent.system());

        setTime(Instant.parse("2024-03-02T12:00:00.000Z").plus(90, ChronoUnit.DAYS).plusMillis(1));
        var request3 = new SendMailRequest();
        request3.sender = new SenderDTO();
        request3.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver3 = new ReceiverDTO();
        receiver3.mail = "max.mustermann@kicherkrabbe.com";
        request3.receivers = Set.of(receiver3);
        request3.subject = "Hello";
        request3.text = "Hello Max";
        sendMail(request3, Agent.system());

        // when: cleaning up old mails
        cleanupOldMails(Agent.system());

        // then: the first two mails are removed since they are older than 90 days
        var mails = getMails(0, 10, Agent.user(AgentId.of("USER_ID")));
        assertThat(mails.total).isEqualTo(1);
        assertThat(mails.mails)
                .extracting(mail -> mail.receivers.iterator().next().mail)
                .containsExactly("max.mustermann@kicherkrabbe.com");
    }

}
