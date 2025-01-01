package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateRateLimitRequest;
import de.bennyboer.kicherkrabbe.mailing.mail.*;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SendMailTest extends MailingModuleTest {

    @Test
    void shouldSendMail() {
        // given: the system user is allowed to send mails
        allowSystemUserToSendMails();

        // and: the current user is allowed to read mails and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: we are at a fixed point in time
        setTime(Instant.parse("2024-12-08T10:15:30.000Z"));

        // and: a request to send a mail
        var request = new SendMailRequest();
        request.sender = new SenderDTO();
        request.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request.receivers = Set.of(receiver1);
        request.subject = "Hello";
        request.text = "Hello John";

        // when: the system user sends the mail
        var result = sendMail(request, Agent.system());

        // then: the mail is sent
        assertThat(result.id).isNotBlank();
        assertThat(result.version).isEqualTo(0L);

        // and: the sent mail is correct
        var mail = getMail(result.id, Agent.user(AgentId.of("USER_ID"))).mail;
        assertThat(mail.sender.mail).isEqualTo("no-reply@kicherkrabbe.com");
        assertThat(mail.receivers).hasSize(1);
        assertThat(mail.receivers.stream().findFirst().orElseThrow().mail).isEqualTo("john.doe@kicherkrabbe.com");
        assertThat(mail.subject).isEqualTo("Hello");
        assertThat(mail.text).isEqualTo("Hello John");
        assertThat(mail.sentAt).isEqualTo(Instant.parse("2024-12-08T10:15:30.000Z"));

        // when: sending the mail via mailing service
        sendMailViaMailingService(result.id, Agent.system());

        // then: the mail is sent via mailing service
        var sentMails = mailApi.getSentMails();
        assertThat(sentMails).hasSize(1);
        var sentMail = sentMails.stream().findFirst().orElseThrow();
        assertThat(sentMail.getSender()).isEqualTo(Sender.of(EMail.of("no-reply@kicherkrabbe.com")));
        assertThat(sentMail.getReceivers()).hasSize(1);
        var receiver = sentMail.getReceivers().stream().findFirst().orElseThrow();
        assertThat(receiver).isEqualTo(Receiver.of(EMail.of("john.doe@kicherkrabbe.com")));
        assertThat(sentMail.getSubject()).isEqualTo(Subject.of("Hello"));
        assertThat(sentMail.getText()).isEqualTo(Text.of("Hello John"));
    }

    @Test
    void shouldNotSendMailWhenRateLimited() {
        // given: the system user is allowed to send mails
        allowSystemUserToSendMails();

        // and: the current user is allowed to read mails and manage settings
        allowUserToReadAndManageSettings("USER_ID");

        // and: the rate limit is set to 1 mail per day
        var settings = getSettings(Agent.user(AgentId.of("USER_ID")));
        var settingsVersion = settings.settings.version;
        var updateRateLimitRequest = new UpdateRateLimitRequest();
        updateRateLimitRequest.version = settingsVersion;
        updateRateLimitRequest.durationInMs = 24 * 60 * 60 * 1000;
        updateRateLimitRequest.limit = 1;
        updateRateLimit(updateRateLimitRequest, Agent.user(AgentId.of("USER_ID")));

        // and: we are at a fixed point in time
        var now = Instant.parse("2024-12-08T10:15:30.000Z");
        setTime(now);

        // and: a mail is sent
        var request = new SendMailRequest();
        request.sender = new SenderDTO();
        request.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request.receivers = Set.of(receiver1);
        request.subject = "Hello";
        request.text = "Hello John";
        sendMail(request, Agent.system());

        // when: another mail is sent; then: an exception is thrown
        var request2 = new SendMailRequest();
        request2.sender = new SenderDTO();
        request2.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver2 = new ReceiverDTO();
        receiver2.mail = "jane.doe@kicherkrabbe.com";
        request2.receivers = Set.of(receiver1);
        request2.subject = "Hello";
        request2.text = "Hello Jane";
        assertThatThrownBy(() -> sendMail(request2, Agent.system()))
                .matches(e -> e instanceof RateLimitExceededError);

        // when: some time has passed
        setTime(now.plusMillis(24 * 60 * 60 * 1000 + 1));

        // and: another mail is sent
        var result = sendMail(request2, Agent.system());

        // then: the mail is sent
        assertThat(result.id).isNotBlank();
    }

    @Test
    void shouldNotSendMailWhenSystemUserDoesNotHavePermission() {
        // given: the system user is not allowed to sent mails

        // and: a request to send a mail
        var request = new SendMailRequest();
        request.sender = new SenderDTO();
        request.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request.receivers = Set.of(receiver1);
        request.subject = "Hello";
        request.text = "Hello John";

        // when: the system user sends the mail; then: an exception is thrown
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAcceptInvalidRequests() {
        // given: the system user is allowed to send mails
        allowSystemUserToSendMails();

        // and: a request to send a mail
        var request = new SendMailRequest();
        request.sender = new SenderDTO();
        request.sender.mail = "no-reply@kicherkrabbe.com";
        var receiver1 = new ReceiverDTO();
        receiver1.mail = "john.doe@kicherkrabbe.com";
        request.receivers = Set.of(receiver1);
        request.subject = "Hello";
        request.text = "Hello John";

        // when: the mail is sent with invalid sender; then: an exception is thrown
        request.sender = null;
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid sender mail; then: an exception is thrown
        request.sender = new SenderDTO();
        request.sender.mail = null;
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid receiver; then: an exception is thrown
        request.sender.mail = "no-reply@kicherkrabbe.com";
        request.receivers = null;
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid receiver mail; then: an exception is thrown
        request.receivers = Set.of();
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid subject; then: an exception is thrown
        request.receivers = Set.of(receiver1);
        request.subject = null;
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid subject; then: an exception is thrown
        request.subject = "";
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid text; then: an exception is thrown
        request.subject = "Hello";
        request.text = null;
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is sent with invalid text; then: an exception is thrown
        request.text = "";
        assertThatThrownBy(() -> sendMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
