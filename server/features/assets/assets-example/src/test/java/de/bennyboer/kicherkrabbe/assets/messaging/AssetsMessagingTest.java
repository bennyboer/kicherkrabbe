package de.bennyboer.kicherkrabbe.assets.messaging;

import de.bennyboer.kicherkrabbe.assets.AssetsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
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

@Import(AssetsMessaging.class)
public class AssetsMessagingTest extends EventListenerTest {

    @MockitoBean
    private AssetsModule module;

    @Autowired
    public AssetsMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.removePermissionsForUser(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToCreateAssets(anyString())).thenReturn(Mono.empty());
        when(module.allowUserToManageAsset(anyString(), anyString())).thenReturn(Mono.empty());
        when(module.removePermissionsOnAsset(anyString())).thenReturn(Mono.empty());
        when(module.allowAnonymousUsersToReadAsset(anyString())).thenReturn(Mono.empty());
        when(module.disallowAnonymousUsersToReadAsset(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateAssetsOnUserCreated() {
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

        // then: the user is allowed to create assets
        verify(module, timeout(5000).times(1)).allowUserToCreateAssets(eq("USER_ID"));
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
    void shouldAllowUserToManageAssetOnAssetCreated() {
        // when: an asset created event is published
        send(
                AggregateType.of("ASSET"),
                AggregateId.of("ASSET_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.user(AgentId.of("USER_ID")),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage the asset
        verify(module, timeout(5000).times(1)).allowUserToManageAsset(eq("ASSET_ID"), eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForAssetOnAssetDeleted() {
        // when: an asset deleted event is published
        send(
                AggregateType.of("ASSET"),
                AggregateId.of("ASSET_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the assets permissions are removed
        verify(module, timeout(5000).times(1)).removePermissionsOnAsset(eq("ASSET_ID"));
    }

    @Test
    void shouldAllowAnonymousUsersToReadAssetOnAssetCreated() {
        // when: an asset created event is published
        send(
                AggregateType.of("ASSET"),
                AggregateId.of("ASSET_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: anonymous users are allowed to read the asset
        verify(module, timeout(5000).times(1)).allowAnonymousUsersToReadAsset(eq("ASSET_ID"));
    }

    @Test
    void shouldDisallowAnonymousUsersToReadAssetOnAssetDeleted() {
        // when: an asset deleted event is published
        send(
                AggregateType.of("ASSET"),
                AggregateId.of("ASSET_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: anonymous users are disallowed to read the asset
        verify(module, timeout(5000).times(1)).disallowAnonymousUsersToReadAsset(eq("ASSET_ID"));
    }

}
