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

public class QueryMailTest extends MailboxModuleTest {

    @Test
    void shouldQueryMail() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // and: a received mail
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
        var result = receiveMail(request, Agent.system());

        // when: the mail is queried
        var mail = getMail(result.mailId, Agent.user(AgentId.of("USER_ID"))).mail;

        // then: the received mail is correct
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
    void shouldNotReadMailWhenHavingNoPermission() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is not allowed to read mails

        // and: a received mail
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
        var result = receiveMail(request, Agent.system());

        // when: the mail is queried; then: an exception is thrown
        assertThatThrownBy(() -> getMail(result.mailId, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldReturnNullIfMailDoesNotExist() {
        // when: a mail is queried that does not exist; then: an exception is thrown
        assertThatThrownBy(() -> getMail("MAIL_ID", Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
