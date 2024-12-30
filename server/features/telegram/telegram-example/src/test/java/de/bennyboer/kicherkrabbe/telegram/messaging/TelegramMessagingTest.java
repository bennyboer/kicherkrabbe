package de.bennyboer.kicherkrabbe.telegram.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.telegram.TelegramModule;
import de.bennyboer.kicherkrabbe.telegram.api.requests.SendMessageViaBotRequest;
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

@Import(TelegramMessaging.class)
public class TelegramMessagingTest extends EventListenerTest {

    @MockBean
    private TelegramModule module;

    @Autowired
    public TelegramMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.sendMessageViaBot(any(), any())).thenReturn(Mono.empty());
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
    void shouldSendMessageViaBotOnNotificationSentWithTelegramChannel() {
        // when: a notification sent event is published with telegram channel
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
                                        "chatId", "CHAT_ID"
                                )
                        ))
                )
        );

        // then: a message is sent via bot
        var request = new SendMessageViaBotRequest();
        request.chatId = "CHAT_ID";
        request.text = """
                __System-Benachrichtigung__: **New mail**
                There is a new mail in your mailbox
                """;
        verify(module, timeout(10000).times(1)).sendMessageViaBot(eq(request), eq(Agent.system()));
    }

    @Test
    void shouldNotSendMessageViaBotOnNotificationSentWithoutTelegramChannel() {
        // when: a notification sent event is published without telegram channel
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
                        ))
                )
        );

        // then: no message is sent via bot
        verify(module, timeout(10000).times(0)).sendMessageViaBot(any(), any());
    }

}
