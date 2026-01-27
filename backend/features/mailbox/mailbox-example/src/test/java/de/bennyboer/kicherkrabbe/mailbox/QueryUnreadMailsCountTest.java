package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsReadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryUnreadMailsCountTest extends MailboxModuleTest {

    @Test
    void shouldQueryUnreadMailsCount() {
        // given: the system user is allowed to receive page
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage page
        allowUserToReadAndManageMails("USER_ID");

        // and: some received page
        var request1 = new ReceiveMailRequest();
        request1.origin = new OriginDTO();
        request1.origin.type = OriginTypeDTO.INQUIRY;
        request1.origin.id = "INQUIRY_ID_1";
        request1.sender = new SenderDTO();
        request1.sender.name = "John Doe";
        request1.sender.mail = "john.doe@kicherkrabbe.com";
        request1.sender.phone = "+49123456789";
        request1.subject = "Subject 1";
        request1.content = "Message 1";
        var result1 = receiveMail(request1, Agent.system());

        var request2 = new ReceiveMailRequest();
        request2.origin = new OriginDTO();
        request2.origin.type = OriginTypeDTO.INQUIRY;
        request2.origin.id = "INQUIRY_ID_2";
        request2.sender = new SenderDTO();
        request2.sender.name = "Jane Doe";
        request2.sender.mail = "jane.doe@kicherkrabbe.com";
        request2.sender.phone = null;
        request2.subject = "Subject 2";
        request2.content = "Message 2";
        var result2 = receiveMail(request2, Agent.system());

        var request3 = new ReceiveMailRequest();
        request3.origin = new OriginDTO();
        request3.origin.type = OriginTypeDTO.INQUIRY;
        request3.origin.id = "INQUIRY_ID_3";
        request3.sender = new SenderDTO();
        request3.sender.name = "Max Mustermann";
        request3.sender.mail = "max.mustermann@kicherkrabbe.com";
        request3.sender.phone = "0282 8383 3848";
        request3.subject = "Subject 3";
        request3.content = "Message 3";
        var result3 = receiveMail(request3, Agent.system());

        // and: the first mail is marked as read
        var markMailAsReadRequest = new MarkMailAsReadRequest();
        markMailAsReadRequest.version = 0L;
        markMailAsRead(result1.mailId, markMailAsReadRequest, Agent.user(AgentId.of("USER_ID")));

        // when: the unread mails count is queried
        var count = getUnreadMailsCount(Agent.user(AgentId.of("USER_ID")));

        // then: the count is 2
        assertThat(count.count).isEqualTo(2);
    }

    @Test
    void shouldNotQueryUnreadMailsCountWhenHavingNoPermission() {
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
        receiveMail(request, Agent.system());

        // when: the unread mails count is queried; then: an exception is thrown
        assertThatThrownBy(() -> getUnreadMailsCount(Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldReturnZeroIfNoMailIsReceivedYet() {
        // given: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // when: the unread mails count is queried
        var count = getUnreadMailsCount(Agent.user(AgentId.of("USER_ID")));

        // then: the count is 0
        assertThat(count.count).isEqualTo(0);
    }

}
