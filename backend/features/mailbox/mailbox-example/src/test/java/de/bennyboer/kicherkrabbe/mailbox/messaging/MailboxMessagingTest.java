package de.bennyboer.kicherkrabbe.mailbox.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.mailbox.MailboxModule;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(MailboxMessaging.class)
public class MailboxMessagingTest extends EventListenerTest {

    @MockitoBean
    private MailboxModule module;

    @Autowired
    public MailboxMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToReadAndManageMails(any())).thenReturn(Mono.empty());
        when(module.allowUsersThatAreAllowedToManageMailsToManageMail(any())).thenReturn(Mono.empty());
        when(module.updateMailInLookup(any())).thenReturn(Mono.empty());
        when(module.removeMailFromLookup(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForMail(any())).thenReturn(Mono.empty());
        when(module.receiveMail(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToReadAndManageMails() {
        // when: a user created event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage inquiries
        verify(module, timeout(5000).times(1)).allowUserToReadAndManageMails(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUser() {
        // when: a user deleted event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the user are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateMailLookupOnReceived() {
        // when: a mail received event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("RECEIVED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the mail is updated in the lookup
        verify(module, timeout(5000).times(1)).updateMailInLookup(eq("MAIL_ID"));
    }

    @Test
    void shouldUpdateMailInLookupOnMarkedAsRead() {
        // when: a mail marked as read event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("MARKED_AS_READ"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the mail is updated in the lookup
        verify(module, timeout(5000).times(1)).updateMailInLookup(eq("MAIL_ID"));
    }

    @Test
    void shouldUpdateMailInLookupOnMarkedAsUnread() {
        // when: a mail marked as unread event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("MARKED_AS_UNREAD"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the mail is updated in the lookup
        verify(module, timeout(5000).times(1)).updateMailInLookup(eq("MAIL_ID"));
    }

    @Test
    void shouldRemoveMailFromLookup() {
        // when: a mail deleted event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the mail is removed from the lookup
        verify(module, timeout(5000).times(1)).removeMailFromLookup(eq("MAIL_ID"));
    }

    @Test
    void shouldRemovePermissionsForDeletedMail() {
        // when: a mail deleted event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the mail are removed
        verify(module, timeout(5000).times(1)).removePermissionsForMail(eq("MAIL_ID"));
    }

    @Test
    void shouldAllowUserToManageInquiry() {
        // when: a mail received event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("RECEIVED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage the mail
        verify(module, timeout(5000).times(1)).allowUsersThatAreAllowedToManageMailsToManageMail(eq("MAIL_ID"));
    }

    @Test
    void shouldReceiveMailOnInquirySent() {
        // when: an inquiry sent event is published
        send(
                AggregateType.of("INQUIRY"),
                AggregateId.of("INQUIRY_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "sender", Map.of(
                                "name", "John Doe",
                                "mail", "john.doe@kicherkrabbe.com",
                                "phone", "+49123456789"
                        ),
                        "subject", "Subject",
                        "message", "Message"
                )
        );

        // then: the mail is received
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
        verify(module, timeout(5000).times(1)).receiveMail(eq(request), eq(Agent.system()));
    }

}
