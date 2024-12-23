package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsReadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsUnreadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailNotUnreadException;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MarkMailAsReadTest extends MailboxModuleTest {

    @Test
    void shouldMarkMailAsRead() {
        // given: the system user is allowed to receive mails
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // and: we are at a specific point in time
        setTime(Instant.parse("2024-03-20T12:30:00.000Z"));

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

        // when: the mail is marked as read at a later point in time
        setTime(Instant.parse("2024-03-20T13:00:00.000Z"));
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        markMailAsRead(result.mailId, markMailAsReadRequest, Agent.user(AgentId.of("USER_ID")));

        // then: the mail is marked as read
        var mail = getMail(result.mailId, Agent.user(AgentId.of("USER_ID"))).mail;
        assertThat(mail.id).isEqualTo(result.mailId);
        assertThat(mail.version).isEqualTo(1L);
        assertThat(mail.origin.type).isEqualTo(OriginTypeDTO.INQUIRY);
        assertThat(mail.origin.id).isEqualTo("INQUIRY_ID");
        assertThat(mail.sender.name).isEqualTo("John Doe");
        assertThat(mail.sender.mail).isEqualTo("john.doe@kicherkrabbe.com");
        assertThat(mail.sender.phone).isEqualTo("+49123456789");
        assertThat(mail.subject).isEqualTo("Subject");
        assertThat(mail.content).isEqualTo("Message");
        assertThat(mail.receivedAt).isEqualTo(Instant.parse("2024-03-20T12:30:00.000Z"));
        assertThat(mail.readAt).isEqualTo(Instant.parse("2024-03-20T13:00:00.000Z"));
    }

    @Test
    void shouldNotMarkMailAsReadGivenNoPermission() {
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

        // when: the mail is marked as read; then: an exception is thrown
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        assertThatThrownBy(() -> markMailAsRead(result.mailId, markMailAsReadRequest, Agent.system()))
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

        // and: the mail is already marked as read
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        markMailAsRead(result.mailId, markMailAsReadRequest, Agent.user(AgentId.of("USER_ID")));

        // and: the mail is marked as unread again
        var markMailAsUnreadRequest = new MarkMailAsUnreadRequest();
        markMailAsUnreadRequest.version = 1L;
        markMailAsUnread(result.mailId, markMailAsUnreadRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the mail is marked as read with an outdated version; then: an exception is thrown
        assertThatThrownBy(() -> deleteMail(result.mailId, 1L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorIfMailIsAlreadyMarkedAsRead() {
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

        // and: the mail is already marked as read
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        markMailAsRead(result.mailId, markMailAsReadRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the mail is marked as read again; then: an exception is thrown
        markMailAsReadRequest.version = 1L;
        assertThatThrownBy(() -> markMailAsRead(
                result.mailId,
                markMailAsReadRequest,
                Agent.user(AgentId.of("USER_ID"))
        )).isInstanceOf(MailNotUnreadException.class);
    }

    @Test
    void shouldRaiseMissingPermissionErrorIfMailDoesNotExist() {
        // when: a mail that does not exist is marked as read; then: an exception is thrown
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        assertThatThrownBy(() -> markMailAsRead("MAIL_ID", markMailAsReadRequest, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
