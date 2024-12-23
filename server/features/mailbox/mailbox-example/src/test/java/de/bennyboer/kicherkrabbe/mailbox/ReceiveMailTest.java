package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReceiveMailTest extends MailboxModuleTest {

    @Test
    void shouldReceiveMail() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // and: a request to receive a mail
        var request = new ReceiveMailRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.INQUIRY;
        request.origin.id = "INQUIRY_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe@kicherkrabbe.com";
        request.sender.phone = "+49123456789";
        request.subject = "Subject";
        request.content = "Message";

        // when: the system user receives the mail
        var result = receiveMail(request, Agent.system());

        // then: the mail is received
        assertThat(result.mailId).isNotBlank();
        assertThat(result.version).isEqualTo(0L);

        // and: the received mail is correct
        var mail = getMail(result.mailId, Agent.user(AgentId.of("USER_ID"))).mail;
        assertThat(mail.id).isEqualTo(result.mailId);
        assertThat(mail.version).isEqualTo(0L);
        assertThat(mail.origin.type).isEqualTo(OriginTypeDTO.INQUIRY);
        assertThat(mail.origin.id).isEqualTo("INQUIRY_ID");
        assertThat(mail.sender.name).isEqualTo("John Doe");
        assertThat(mail.sender.mail).isEqualTo("john.doe@kicherkrabbe.com");
        assertThat(mail.sender.phone).isEqualTo("+49123456789");
        assertThat(mail.subject).isEqualTo("Subject");
        assertThat(mail.content).isEqualTo("Message");
        assertThat(mail.receivedAt).isNotNull();
        assertThat(mail.readAt).isNull();
    }

    @Test
    void shouldNotReceiveMailWhenSystemUserDoesNotHavePermission() {
        // given: the system user is not allowed to receive mails

        // and: a request to receive a mail
        var request = new ReceiveMailRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.INQUIRY;
        request.origin.id = "INQUIRY_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe@kicherkrabbe.com";
        request.sender.phone = "+49123456789";
        request.subject = "Subject";
        request.content = "Message";

        // when: the system user receives the mail; then: an exception is thrown
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAcceptInvalidRequests() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: a request to receive a mail with an invalid origin
        var request = new ReceiveMailRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.INQUIRY;
        request.origin.id = "INQUIRY_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe@kicherkrabbe.com";
        request.sender.phone = "+49123456789";
        request.subject = "Subject";
        request.content = "Message";

        // when: the mail is received with invalid origin ID; then: an exception is thrown
        request.origin.id = null;
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.origin.id = "INQUIRY_ID";

        // when: the mail is received with invalid origin ID; then: an exception is thrown
        request.origin.id = "";
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.origin.id = "INQUIRY_ID";

        // when: the mail is received with invalid origin type; then: an exception is thrown
        request.origin.type = null;
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.origin.type = OriginTypeDTO.INQUIRY;

        // when: the mail is received with invalid sender name; then: an exception is thrown
        request.sender.name = null;
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.sender.name = "John Doe";

        // when: the mail is received with invalid sender name; then: an exception is thrown
        request.sender.name = "";
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.sender.name = "John Doe";

        // when: the mail is received with invalid sender mail; then: an exception is thrown
        request.sender.mail = null;
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.sender.mail = "john.doe@kicherkrabbe.com";

        // when: the mail is received with invalid sender mail; then: an exception is thrown
        request.sender.mail = "";
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.sender.mail = "john.doe@kicherkrabbe.com";

        // when: the mail is received with invalid sender mail; then: an exception is thrown
        request.sender.mail = "Test";
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.sender.mail = "john.doe@kicherkrabbe.com";

        // when: the mail is received with an invalid subject; then: an exception is thrown
        request.subject = null;
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.subject = "Subject";

        // when: the mail is received with an invalid subject; then: an exception is thrown
        request.subject = "";
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
        request.subject = "Subject";

        // when: the mail is received with an invalid content; then: an exception is thrown
        request.content = null;
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the mail is received with an invalid content; then: an exception is thrown
        request.content = "";
        assertThatThrownBy(() -> receiveMail(request, Agent.system()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
