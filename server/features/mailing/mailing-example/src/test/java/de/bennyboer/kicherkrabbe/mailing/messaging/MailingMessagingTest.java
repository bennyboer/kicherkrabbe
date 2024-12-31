package de.bennyboer.kicherkrabbe.mailing.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.mailing.MailingModule;
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
                        )
                )
        );

        // then: a message is sent via bot
        var request = new SendMailRequest();
        request.mail = "john.doe@kicherkrabbe.com";
        request.subject = "System-Benachrichtigung: New mail";
        request.text = "There is a new mail in your mailbox";
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
