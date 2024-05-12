package de.bennyboer.kicherkrabbe.fabrics.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.fabrics.FabricsModule;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(FabricsMessaging.class)
public class FabricsMessagingTest extends EventListenerTest {

    @MockBean
    private FabricsModule module;

    @Autowired
    public FabricsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateFabrics(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.updateFabricInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removeFabricFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManageFabric(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsOnFabric(anyString())).thenReturn(Mono.empty());
        when(module.allowAnonymousAndSystemUsersToReadPublishedFabric(anyString())).thenReturn(Mono.empty());
        when(module.disallowAnonymousAndSystemUsersToReadPublishedFabric(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateFabricsOnUserCreated() {
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

        // then: the user is allowed to create fabrics
        verify(module, timeout(5000).times(1)).allowUserToCreateFabrics(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUserOnUserDeleted() {
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
    void shouldUpdateFabricInLookupOnFabricCreatedOrUpdated() {
        // when: some fabric events are published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(2),
                EventName.of("RENAMED"),
                Version.of(1),
                Agent.system(),
                Instant.now(),
                Map.of()
        );
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(3),
                EventName.of("DELETED"),
                Version.of(2),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric is only updated on non-deleted events
        verify(module, timeout(5000).times(2)).updateFabricInLookup(eq("FABRIC_ID"));
    }

    @Test
    void shouldRemoveFabricFromLookupOnFabricDeleted() {
        // when: a fabric deleted event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric is removed from the lookup
        verify(module, timeout(5000).times(1)).removeFabricFromLookup(eq("FABRIC_ID"));
    }

    @Test
    void shouldAllowUserToManageFabricsOnFabricCreated() {
        // when: a fabric created event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage fabrics
        verify(module, timeout(5000).times(1)).allowUserToManageFabric(eq("FABRIC_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsOnFabricDeleted() {
        // when: a fabric deleted event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions on the fabric are removed
        verify(module, timeout(5000).times(1)).removePermissionsOnFabric(eq("FABRIC_ID"));
    }

    @Test
    void shouldAllowAnonymousAndSystemUsersToReadPublishedFabricOnFabricPublished() {
        // when: a fabric published event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("PUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the group is allowed to read the published fabric
        verify(module, timeout(5000).times(1)).allowAnonymousAndSystemUsersToReadPublishedFabric(eq("FABRIC_ID"));
    }

    @Test
    void shouldDisallowAnonymousAndSystemUsersToReadPublishedFabricOnFabricUnpublished() {
        // when: a fabric unpublished event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("UNPUBLISHED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the groups are disallowed to read the published fabric
        verify(module, timeout(5000).times(1)).disallowAnonymousAndSystemUsersToReadPublishedFabric(eq("FABRIC_ID"));
    }

}
