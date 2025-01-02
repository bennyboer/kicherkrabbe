package de.bennyboer.kicherkrabbe.users.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.users.UsersModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(UsersMessaging.class)
public class UsersMessagingTest extends EventListenerTest {

    @MockitoBean
    private UsersModule module;

    @Autowired
    public UsersMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.updateUserInLookup(eq("USER_ID"))).thenReturn(Mono.empty());
        when(module.addPermissionsForNewUser(eq("USER_ID"))).thenReturn(Mono.empty());
        when(module.removeUserFromLookup(eq("USER_ID"))).thenReturn(Mono.empty());
        when(module.removePermissionsOnUser(eq("USER_ID"))).thenReturn(Mono.empty());
    }

    @Test
    void shouldUpdateUsersLookupOnUserCreated() {
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

        // then: the users lookup is updated
        verify(module, timeout(5000).times(1)).updateUserInLookup(eq("USER_ID"));
    }

    @Test
    void shouldAddPermissionsForNewUserOnUserCreated() {
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

        // then: the permissions are added for the new user
        verify(module, timeout(5000).times(1)).addPermissionsForNewUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateUsersLookupOnUserDeleted() {
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

        // then: the users lookup is updated
        verify(module, timeout(5000).times(1)).removeUserFromLookup(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsOnUserDeleted() {
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

        // then: the permissions are removed for the user
        verify(module, timeout(5000).times(1)).removePermissionsOnUser(eq("USER_ID"));
    }

}
