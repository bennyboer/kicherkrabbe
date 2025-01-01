package de.bennyboer.kicherkrabbe.mailing.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.mailing.MailingModule;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(MailingMessaging.class)
public class MailingMessagingTest extends EventListenerTest {

    @MockBean
    private MailingModule module;

    @Autowired
    public MailingMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.sendMail(any(), any())).thenReturn(Mono.empty());
        when(module.allowUserToReadAndManageSettings(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateMailInLookup(any())).thenReturn(Mono.empty());
        when(module.removeMailFromLookup(any())).thenReturn(Mono.empty());
        when(module.allowSystemUserToDeleteMail(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForMail(any())).thenReturn(Mono.empty());
        when(module.allowUsersThatAreAllowedToReadMailsToReadMail(any())).thenReturn(Mono.empty());
        when(module.sendMailViaMailingService(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToReadAndManageSettings() {
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

        // then: the user is allowed to read and manage settings
        verify(module, timeout(10000).times(1)).allowUserToReadAndManageSettings(eq("USER_ID"));
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
        verify(module, timeout(10000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateMailingMailLookupOnSent() {
        // when: a mail sent event is published
        send(
                AggregateType.of("MAILING_MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the mail is updated in the lookup
        verify(module, timeout(10000).times(1)).updateMailInLookup(eq("MAIL_ID"));
    }

    @Test
    void shouldRemoveMailingMailFromLookup() {
        // when: a mail deleted event is published
        send(
                AggregateType.of("MAILING_MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the mail is removed from the lookup
        verify(module, timeout(10000).times(1)).removeMailFromLookup(eq("MAIL_ID"));
    }

    @Test
    void shouldAllowSystemUserToDeleteMailingMailOnSent() {
        // when: a mail sent event is published
        send(
                AggregateType.of("MAILING_MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the system user is allowed to delete the mail
        verify(module, timeout(10000).times(1)).allowSystemUserToDeleteMail(eq("MAIL_ID"));
    }

    @Test
    void shouldAllowUsersThatAreAllowedToReadMailsToReadMailOnMailSent() {
        // when: a mail sent event is published
        send(
                AggregateType.of("MAILING_MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the users that are allowed to read mails are allowed to read the mail
        verify(module, timeout(10000).times(1)).allowUsersThatAreAllowedToReadMailsToReadMail(eq("MAIL_ID"));
    }

    @Test
    void shouldRemovePermissionsForDeletedMailingMail() {
        // when: a mail deleted event is published
        send(
                AggregateType.of("MAILING_MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the mail are removed
        verify(module, timeout(10000).times(1)).removePermissionsForMail(eq("MAIL_ID"));
    }

    @Test
    void shouldSendMailViaMailingServiceOnMailSent() {
        // when: a mail sent event is published
        send(
                AggregateType.of("MAILING_MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: a mail is sent via mailing service
        verify(module, timeout(10000).times(1)).sendMailViaMailingService(eq("MAIL_ID"), eq(Agent.system()));
    }

    @Test
    void shouldSendMailOnNotificationSentWithEmailChannel() {
        // when: a notification sent event is published with email channel
        send(
                AggregateType.of("NOTIFICATION"),
                AggregateId.of("NOTIFICATION_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "title", "New mail",
                        "message", "There is a new mail in your mailbox",
                        "channels", List.of(Map.of(
                                        "type", "EMAIL",
                                        "mail", "john.doe@kicherkrabbe.com"
                                )
                        ),
                        "origin", Map.of(
                                "type", "MAIL",
                                "id", "SOME_MAIL_ID"
                        )
                )
        );

        // then: a message is sent via bot
        var request = new SendMailRequest();
        request.sender = new SenderDTO();
        request.sender.mail = "no-reply@kicherkrabbe.com";
        request.receivers = new HashSet<>();
        var receiver = new ReceiverDTO();
        receiver.mail = "john.doe@kicherkrabbe.com";
        request.receivers.add(receiver);
        request.subject = "System-Benachrichtigung: New mail";
        request.text = "There is a new mail in your mailbox: https://kicherkrabbe.com/admin/mailbox/SOME_MAIL_ID";
        verify(module, timeout(10000).times(1)).sendMail(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldNotSendMailOnNotificationSentWithoutEmailChannel() {
        // when: a notification sent event is published without email channel
        send(
                AggregateType.of("NOTIFICATION"),
                AggregateId.of("NOTIFICATION_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "title", "New mail",
                        "message", "There is a new mail in your mailbox",
                        "channels", List.of(Map.of(
                                "type", "TELEGRAM",
                                "telegram", Map.of(
                                        "chatId", "1234567890"
                                )
                        ))
                )
        );

        // then: no message is sent via bot
        verify(module, timeout(10000).times(0)).sendMail(any(), any());
    }

}
