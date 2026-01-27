package de.bennyboer.kicherkrabbe.notifications.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.notifications.NotificationsModule;
import de.bennyboer.kicherkrabbe.notifications.api.OriginDTO;
import de.bennyboer.kicherkrabbe.notifications.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetTypeDTO;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
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

@Import(NotificationsMessaging.class)
public class NotificationsMessagingTest extends EventListenerTest {

    @MockitoBean
    private NotificationsModule module;

    @Autowired
    public NotificationsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToReadNotificationsAndManageSettings(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateNotificationInLookup(any())).thenReturn(Mono.empty());
        when(module.removeNotificationFromLookup(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForNotification(any())).thenReturn(Mono.empty());
        when(module.sendNotification(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToReadNotificationsAndManageSettings() {
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

        // then: the user is allowed to read notifications and manage settings
        verify(module, timeout(5000).times(1)).allowUserToReadNotificationsAndManageSettings(eq("USER_ID"));
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
    void shouldUpdateNotificationLookupOnSent() {
        // when: a notification sent event is published
        send(
                AggregateType.of("NOTIFICATION"),
                AggregateId.of("NOTIFICATION_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the notification is updated in the lookup
        verify(module, timeout(5000).times(1)).updateNotificationInLookup(eq("NOTIFICATION_ID"));
    }

    @Test
    void shouldRemoveNotificationFromLookup() {
        // when: a notification deleted event is published
        send(
                AggregateType.of("NOTIFICATION"),
                AggregateId.of("NOTIFICATION_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the notification is removed from the lookup
        verify(module, timeout(5000).times(1)).removeNotificationFromLookup(eq("NOTIFICATION_ID"));
    }

    @Test
    void shouldAllowSystemUserToDeleteNotificationOnSent() {
        // when: a notification sent event is published
        send(
                AggregateType.of("NOTIFICATION"),
                AggregateId.of("NOTIFICATION_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the system user is allowed to delete the notification
        verify(module, timeout(5000).times(1)).allowSystemUserToDeleteNotification(eq("NOTIFICATION_ID"));
    }

    @Test
    void shouldRemovePermissionsForDeletedNotification() {
        // when: a notification deleted event is published
        send(
                AggregateType.of("NOTIFICATION"),
                AggregateId.of("NOTIFICATION_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the notification are removed
        verify(module, timeout(5000).times(1)).removePermissionsForNotification(eq("NOTIFICATION_ID"));
    }

    @Test
    void shouldSendNotificationOnMailReceived() {
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

        // then: a notification is sent
        var request = new SendNotificationRequest();
        request.origin = new OriginDTO();
        request.origin.type = OriginTypeDTO.MAIL;
        request.origin.id = "MAIL_ID";
        request.target = new TargetDTO();
        request.target.type = TargetTypeDTO.SYSTEM;
        request.target.id = "SYSTEM";
        request.title = "Neue Nachricht im Postfach";
        request.message = "Es ist eine neue Nachricht im Postfach eingegangen.";
        verify(module, timeout(5000).times(1)).sendNotification(eq(request), eq(Agent.system()));
    }

}
