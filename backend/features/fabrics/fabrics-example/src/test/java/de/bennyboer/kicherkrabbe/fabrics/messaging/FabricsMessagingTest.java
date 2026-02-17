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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(FabricsMessaging.class)
public class FabricsMessagingTest extends EventListenerTest {

    @MockitoBean
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
        when(module.allowAnonymousAndSystemUsersToReadFeaturedFabric(anyString())).thenReturn(Mono.empty());
        when(module.disallowAnonymousAndSystemUsersToReadFeaturedFabric(anyString())).thenReturn(Mono.empty());
        when(module.removeFabricTypeFromFabrics(anyString(), any())).thenReturn(Flux.empty());
        when(module.removeTopicFromFabrics(anyString(), any())).thenReturn(Flux.empty());
        when(module.removeColorFromFabrics(anyString(), any())).thenReturn(Flux.empty());
        when(module.markTopicAsAvailable(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.markTopicAsUnavailable(anyString())).thenReturn(Mono.empty());
        when(module.markColorAsAvailable(
                anyString(),
                anyString(),
                anyInt(),
                anyInt(),
                anyInt()
        )).thenReturn(Mono.empty());
        when(module.markColorAsUnavailable(anyString())).thenReturn(Mono.empty());
        when(module.markFabricTypeAsAvailable(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.markFabricTypeAsUnavailable(anyString())).thenReturn(Mono.empty());
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
    void shouldUpdateFabricInLookupOnFabricCreated() {
        // when: a fabric created event is published
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

        // then: the fabric is updated in the lookup
        verify(module, timeout(5000).times(1)).updateFabricInLookup(eq("FABRIC_ID"));
    }

    @Test
    void shouldUpdateFabricInLookupOnFabricUpdated() {
        // when: a fabric renamed event is published
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

        // then: the fabric is updated in the lookup
        verify(module, timeout(5000).times(1)).updateFabricInLookup(eq("FABRIC_ID"));
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

    @Test
    void shouldUpdateFabricInLookupOnFabricFeatured() {
        // when: a fabric featured event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("FEATURED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric is updated in the lookup
        verify(module, timeout(5000).times(1)).updateFabricInLookup(eq("FABRIC_ID"));
    }

    @Test
    void shouldUpdateFabricInLookupOnFabricUnfeatured() {
        // when: a fabric unfeatured event is published
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("UNFEATURED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the fabric is updated in the lookup
        verify(module, timeout(5000).times(1)).updateFabricInLookup(eq("FABRIC_ID"));
    }

    @Test
    void shouldRemoveFabricTypeFromFabricsOnFabricTypeDeleted() {
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

        // then: the fabric type is removed from the fabrics
        verify(module, timeout(5000).times(1)).removeFabricTypeFromFabrics(eq("FABRIC_TYPE_ID"), eq(Agent.system()));
    }

    @Test
    void shouldRemoveTopicFromFabricsOnTopicDeleted() {
        // when: a topic deleted event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the topic is removed from the fabrics
        verify(module, timeout(5000).times(1)).removeTopicFromFabrics(eq("TOPIC_ID"), eq(Agent.system()));
    }

    @Test
    void shouldRemoveColorFromFabricsOnColorDeleted() {
        // when: a color deleted event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the color is removed from the fabrics
        verify(module, timeout(5000).times(1)).removeColorFromFabrics(eq("COLOR_ID"), eq(Agent.system()));
    }

    @Test
    void shouldMarkTopicAsAvailableOnTopicCreated() {
        // when: a topic created event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "Topic name")
        );

        // then: the topic is marked as available
        verify(module, timeout(5000).times(1)).markTopicAsAvailable(eq("TOPIC_ID"), eq("Topic name"));
    }

    @Test
    void shouldMarkTopicAsAvailableOnTopicUpdated() {
        // when: a topic updated event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "New topic name")
        );

        // then: the topic is marked as available
        verify(module, timeout(5000).times(1)).markTopicAsAvailable(eq("TOPIC_ID"), eq("New topic name"));
    }

    @Test
    void shouldMarkTopicAsUnavailableOnTopicDeleted() {
        // when: a topic deleted event is published
        send(
                AggregateType.of("TOPIC"),
                AggregateId.of("TOPIC_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the topic is marked as unavailable
        verify(module, timeout(5000).times(1)).markTopicAsUnavailable(eq("TOPIC_ID"));
    }

    @Test
    void shouldMarkColorAsAvailableOnColorCreated() {
        // when: a color created event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "Color name",
                        "red", 255,
                        "green", 0,
                        "blue", 0
                )
        );

        // then: the color is marked as available
        verify(module, timeout(5000).times(1)).markColorAsAvailable(
                eq("COLOR_ID"),
                eq("Color name"),
                eq(255),
                eq(0),
                eq(0)
        );
    }

    @Test
    void shouldMarkColorAsAvailableOnColorUpdated() {
        // when: a color updated event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "name", "New color name",
                        "red", 0,
                        "green", 255,
                        "blue", 0
                )
        );

        // then: the color is marked as available
        verify(module, timeout(5000).times(1)).markColorAsAvailable(
                eq("COLOR_ID"),
                eq("New color name"),
                eq(0),
                eq(255),
                eq(0)
        );
    }

    @Test
    void shouldMarkColorAsUnavailableOnColorDeleted() {
        // when: a color deleted event is published
        send(
                AggregateType.of("COLOR"),
                AggregateId.of("COLOR_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the color is marked as unavailable
        verify(module, timeout(5000).times(1)).markColorAsUnavailable(eq("COLOR_ID"));
    }

    @Test
    void shouldMarkFabricTypeAsAvailableOnFabricTypeCreated() {
        // when: a fabric type created event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "Fabric type name")
        );

        // then: the fabric type is marked as available
        verify(module, timeout(5000).times(1)).markFabricTypeAsAvailable(eq("FABRIC_TYPE_ID"), eq("Fabric type name"));
    }

    @Test
    void shouldMarkFabricTypeAsAvailableOnFabricTypeUpdated() {
        // when: a fabric type updated event is published
        send(
                AggregateType.of("FABRIC_TYPE"),
                AggregateId.of("FABRIC_TYPE_ID"),
                Version.of(1),
                EventName.of("UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "New fabric type name")
        );

        // then: the fabric type is marked as available
        verify(module, timeout(5000).times(1)).markFabricTypeAsAvailable(
                eq("FABRIC_TYPE_ID"),
                eq("New fabric type name")
        );
    }

    @Test
    void shouldMarkFabricTypeAsUnavailableOnFabricTypeDeleted() {
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

        // then: the fabric type is marked as unavailable
        verify(module, timeout(5000).times(1)).markFabricTypeAsUnavailable(eq("FABRIC_TYPE_ID"));
    }

}
