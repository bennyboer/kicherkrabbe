package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryMailsTest extends MailboxModuleTest {

    @Test
    void shouldQueryMails() {
        // given: the system user is allowed to receive page
        allowSystemUserToReceiveMails();

        // and: the current user is allowed to read and manage page
        allowUserToReadAndManageMails("USER_ID");

        // and: we are at a certain point in time
        setTime(Instant.parse("2024-12-14T12:00:00.000Z"));

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
        request1.content = "Message 2";
        var result1 = receiveMail(request1, Agent.system());

        setTime(Instant.parse("2024-12-14T12:30:00.000Z"));
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

        setTime(Instant.parse("2024-12-14T12:45:00.000Z"));
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

        // when: the mails are queried
        var page = getMails("", null, 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the received mails are correct in the correct order
        assertThat(page.total).isEqualTo(3);
        assertThat(page.mails).hasSize(3);

        var mail1 = page.mails.get(0);
        assertThat(mail1.id).isEqualTo(result3.mailId);
        assertThat(mail1.origin.type).isEqualTo(OriginTypeDTO.INQUIRY);
        assertThat(mail1.origin.id).isEqualTo("INQUIRY_ID_3");
        assertThat(mail1.sender.name).isEqualTo("Max Mustermann");
        assertThat(mail1.sender.mail).isEqualTo("max.mustermann@kicherkrabbe.com");
        assertThat(mail1.sender.phone).isEqualTo("0282 8383 3848");
        assertThat(mail1.subject).isEqualTo("Subject 3");
        assertThat(mail1.content).isEqualTo("Message 3");
        assertThat(mail1.receivedAt).isEqualTo(Instant.parse("2024-12-14T12:45:00.000Z"));
        assertThat(mail1.readAt).isNull();

        var mail2 = page.mails.get(1);
        assertThat(mail2.id).isEqualTo(result2.mailId);
        assertThat(mail2.origin.type).isEqualTo(OriginTypeDTO.INQUIRY);
        assertThat(mail2.origin.id).isEqualTo("INQUIRY_ID_2");
        assertThat(mail2.sender.name).isEqualTo("Jane Doe");
        assertThat(mail2.sender.mail).isEqualTo("jane.doe@kicherkrabbe.com");
        assertThat(mail2.sender.phone).isNull();
        assertThat(mail2.subject).isEqualTo("Subject 2");
        assertThat(mail2.content).isEqualTo("Message 2");
        assertThat(mail2.receivedAt).isEqualTo(Instant.parse("2024-12-14T12:30:00.000Z"));
        assertThat(mail2.readAt).isNull();

        var mail3 = page.mails.get(2);
        assertThat(mail3.id).isEqualTo(result1.mailId);
        assertThat(mail3.origin.type).isEqualTo(OriginTypeDTO.INQUIRY);
        assertThat(mail3.origin.id).isEqualTo("INQUIRY_ID_1");
        assertThat(mail3.sender.name).isEqualTo("John Doe");
        assertThat(mail3.sender.mail).isEqualTo("john.doe@kicherkrabbe.com");
        assertThat(mail3.sender.phone).isEqualTo("+49123456789");
        assertThat(mail3.subject).isEqualTo("Subject 1");
        assertThat(mail3.content).isEqualTo("Message 2");
        assertThat(mail3.receivedAt).isEqualTo(Instant.parse("2024-12-14T12:00:00.000Z"));
        assertThat(mail3.readAt).isNull();
    }

    @Test
    void shouldNotQueryMailsWhenHavingNoPermission() {
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

        // when: the mails are queried; then: an exception is thrown
        assertThatThrownBy(() -> getMails("", null, 0, 10, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldReturnEmptyListIfNoMailIsReceivedYet() {
        // given: the current user is allowed to read and manage mails
        allowUserToReadAndManageMails("USER_ID");

        // when: querying mails
        var result = getMails("", null, 0, 10, Agent.user(AgentId.of("USER_ID")));

        // then: the result is an empty list
        assertThat(result.total).isEqualTo(0);
        assertThat(result.mails).isEmpty();
    }

}
