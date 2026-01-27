package de.bennyboer.kicherkrabbe.fabrictypes.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypesModule;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(FabricTypesMessaging.class)
public class FabricTypesMessagingTest extends EventListenerTest {

    @MockitoBean
    private FabricTypesModule module;

    @Autowired
    public FabricTypesMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToCreateFabricTypes(anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.updateFabricTypeInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removeFabricTypeFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManageFabricType(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsForFabricType(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateFabricTypesOnUserCreated() {
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

        // then: the user is allowed to create fabric types
        verify(module, timeout(5000).times(1)).allowUserToCreateFabricTypes(eq("USER_ID"));
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

        // then: the users permissions are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateFabricTypeInLookupOnFabricTypeCreated() {
        // when: a fabric type created event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric type is updated in the lookup
        verify(module, timeout(5000).times(1)).updateFabricTypeInLookup(eq("FABRIC_TYPE_ID"));
    }

    @Test
    void shouldUpdateFabricTypeInLookupOnFabricTypeUpdated() {
        // when: a fabric type updated event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric type is updated in the lookup
        verify(module, timeout(5000).times(1)).updateFabricTypeInLookup(eq("FABRIC_TYPE_ID"));
    }

    @Test
    void shouldRemoveFabricTypeFromLookupOnFabricTypeDeleted() {
        // when: a fabric type deleted event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric type is removed from the lookup
        verify(module, timeout(5000).times(1)).removeFabricTypeFromLookup(eq("FABRIC_TYPE_ID"));
    }

    @Test
    void shouldAllowUserToManageFabricTypeOnFabricTypeCreated() {
        // when: a fabric type created event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage the fabric type
        verify(module, timeout(5000).times(1)).allowUserToManageFabricType(eq("FABRIC_TYPE_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForFabricTypeOnFabricTypeDeleted() {
        // when: a fabric type deleted event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric types permissions are removed
        verify(module, timeout(5000).times(1)).removePermissionsForFabricType(eq("FABRIC_TYPE_ID"));
    }

}
