package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsReadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteMailTest extends MailboxModuleTest {

    @Test
    void shouldDeleteMail() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // and: a mail is received
        var receiveRequest = new ReceiveMailRequest();
        receiveRequest.origin = new OriginDTO();
        receiveRequest.origin.type = OriginTypeDTO.INQUIRY;
        receiveRequest.origin.id = "INQUIRY_ID";
        receiveRequest.sender = new SenderDTO();
        receiveRequest.sender.name = "John Doe";
        receiveRequest.sender.mail = "john.doe@kicherkrabbe.com";
        receiveRequest.sender.phone = "+49123456789";
        receiveRequest.subject = "Subject";
        receiveRequest.content = "Message";
        var result = receiveMail(receiveRequest, Agent.system());

        // when: the mail is deleted
        deleteMail(result.mailId, 0L, Agent.user(AgentId.of("USER_ID")));

        // then: the mail cannot be found anymore since the permission is gone
        assertThatThrownBy(() -> getMail(result.mailId, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotDeleteMailGivenNoPermission() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: a mail is received
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

        // when: the mail is deleted; then: an exception is thrown
        assertThatThrownBy(() -> deleteMail(result.mailId, 0L, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseErrorWhenTheVersionIsNotUpToDate() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // and: a mail is received
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

        // and: the mail is marked as read
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        markMailAsRead(result.mailId, markMailAsReadRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the mail is deleted with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> deleteMail(result.mailId, 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldAllowNoFurtherOperationsOnDeletedMail() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // and: a mail is received
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

        // when: the mail is deleted
        deleteMail(result.mailId, 0L, Agent.user(AgentId.of("USER_ID")));

        // and: the mail is marked as read; then: an exception is thrown
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 1L;
        assertThatThrownBy(() -> markMailAsRead(
                result.mailId,
                markMailAsReadRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfMailDoesNotExist() {
        // when: a mail that does not exist is deleted; then: an exception is thrown
        assertThatThrownBy(() -> deleteMail("MAIL_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
