package de.bennyboer.kicherkrabbe.assets.messaging;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.AssetsModule;
import de.bennyboer.kicherkrabbe.assets.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.AssetResourceId;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
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
        when(module.updateAssetInLookup(anyString())).thenReturn(Mono.empty());
        when(module.removeAssetFromLookup(anyString())).thenReturn(Mono.empty());
        when(module.updateAssetReferences(any(), any(), any())).thenReturn(Mono.empty());
        when(module.updateAssetReferences(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(module.updateResourceNameInReferences(any(), any(), anyString())).thenReturn(Mono.empty());
        when(module.removeAssetReferencesByResource(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToCreateAssetsOnUserCreated() {
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

        verify(module, timeout(5000).times(1)).allowUserToCreateAssets(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUserOnUserDeleted() {
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

        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldAllowUserToManageAssetOnAssetCreated() {
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

        verify(module, timeout(5000).times(1)).allowUserToManageAsset(eq("ASSET_ID"), eq("USER_ID"));
    }

    @Test
    void shouldUpdateLookupOnAssetCreated() {
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

        verify(module, timeout(5000).times(1)).updateAssetInLookup(eq("ASSET_ID"));
    }

    @Test
    void shouldRemovePermissionsForAssetOnAssetDeleted() {
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

        verify(module, timeout(5000).times(1)).removePermissionsOnAsset(eq("ASSET_ID"));
    }

    @Test
    void shouldRemoveLookupOnAssetDeleted() {
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

        verify(module, timeout(5000).times(1)).removeAssetFromLookup(eq("ASSET_ID"));
    }

    @Test
    void shouldAllowAnonymousUsersToReadAssetOnAssetCreated() {
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

        verify(module, timeout(5000).times(1)).allowAnonymousUsersToReadAsset(eq("ASSET_ID"));
    }

    @Test
    void shouldDisallowAnonymousUsersToReadAssetOnAssetDeleted() {
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

        verify(module, timeout(5000).times(1)).disallowAnonymousUsersToReadAsset(eq("ASSET_ID"));
    }

    @Test
    void shouldUpdateAssetReferencesOnFabricCreated() {
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("image", "ASSET_1", "name", "Blumentraum")
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.FABRIC),
                eq(AssetResourceId.of("FABRIC_ID")),
                eq(Set.of(AssetId.of("ASSET_1"))),
                eq("Blumentraum")
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnFabricImageUpdated() {
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(2),
                EventName.of("IMAGE_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("image", "ASSET_2")
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.FABRIC),
                eq(AssetResourceId.of("FABRIC_ID")),
                eq(Set.of(AssetId.of("ASSET_2")))
        );
    }

    @Test
    void shouldUpdateResourceNameOnFabricRenamed() {
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(3),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "New Fabric Name")
        );

        verify(module, timeout(5000).times(1)).updateResourceNameInReferences(
                eq(AssetReferenceResourceType.FABRIC),
                eq(AssetResourceId.of("FABRIC_ID")),
                eq("New Fabric Name")
        );
    }

    @Test
    void shouldRemoveAssetReferencesOnFabricDeleted() {
        send(
                AggregateType.of("FABRIC"),
                AggregateId.of("FABRIC_ID"),
                Version.of(3),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeAssetReferencesByResource(
                eq(AssetReferenceResourceType.FABRIC),
                eq(AssetResourceId.of("FABRIC_ID"))
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnPatternCreated() {
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("images", List.of("ASSET_1", "ASSET_2"), "name", "Babyshirt")
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.PATTERN),
                eq(AssetResourceId.of("PATTERN_ID")),
                eq(Set.of(AssetId.of("ASSET_1"), AssetId.of("ASSET_2"))),
                eq("Babyshirt")
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnPatternImagesUpdated() {
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(2),
                EventName.of("IMAGES_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("images", List.of("ASSET_3"))
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.PATTERN),
                eq(AssetResourceId.of("PATTERN_ID")),
                eq(Set.of(AssetId.of("ASSET_3")))
        );
    }

    @Test
    void shouldUpdateResourceNameOnPatternRenamed() {
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(3),
                EventName.of("RENAMED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("name", "New Pattern Name")
        );

        verify(module, timeout(5000).times(1)).updateResourceNameInReferences(
                eq(AssetReferenceResourceType.PATTERN),
                eq(AssetResourceId.of("PATTERN_ID")),
                eq("New Pattern Name")
        );
    }

    @Test
    void shouldRemoveAssetReferencesOnPatternDeleted() {
        send(
                AggregateType.of("PATTERN"),
                AggregateId.of("PATTERN_ID"),
                Version.of(3),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeAssetReferencesByResource(
                eq(AssetReferenceResourceType.PATTERN),
                eq(AssetResourceId.of("PATTERN_ID"))
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnProductCreated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("images", List.of("ASSET_1"), "number", "P-001")
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.PRODUCT),
                eq(AssetResourceId.of("PRODUCT_ID")),
                eq(Set.of(AssetId.of("ASSET_1"))),
                eq("P-001")
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnProductImagesUpdated() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(2),
                EventName.of("IMAGES_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("images", List.of("ASSET_2", "ASSET_3"))
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.PRODUCT),
                eq(AssetResourceId.of("PRODUCT_ID")),
                eq(Set.of(AssetId.of("ASSET_2"), AssetId.of("ASSET_3")))
        );
    }

    @Test
    void shouldRemoveAssetReferencesOnProductDeleted() {
        send(
                AggregateType.of("PRODUCT"),
                AggregateId.of("PRODUCT_ID"),
                Version.of(3),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeAssetReferencesByResource(
                eq(AssetReferenceResourceType.PRODUCT),
                eq(AssetResourceId.of("PRODUCT_ID"))
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnHighlightCreated() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("imageId", "ASSET_1", "sortOrder", 0)
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.HIGHLIGHT),
                eq(AssetResourceId.of("HIGHLIGHT_ID")),
                eq(Set.of(AssetId.of("ASSET_1"))),
                eq("")
        );
    }

    @Test
    void shouldUpdateAssetReferencesOnHighlightImageUpdated() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(2),
                EventName.of("IMAGE_UPDATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of("imageId", "ASSET_2")
        );

        verify(module, timeout(5000).times(1)).updateAssetReferences(
                eq(AssetReferenceResourceType.HIGHLIGHT),
                eq(AssetResourceId.of("HIGHLIGHT_ID")),
                eq(Set.of(AssetId.of("ASSET_2")))
        );
    }

    @Test
    void shouldRemoveAssetReferencesOnHighlightDeleted() {
        send(
                AggregateType.of("HIGHLIGHT"),
                AggregateId.of("HIGHLIGHT_ID"),
                Version.of(3),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        verify(module, timeout(5000).times(1)).removeAssetReferencesByResource(
                eq(AssetReferenceResourceType.HIGHLIGHT),
                eq(AssetResourceId.of("HIGHLIGHT_ID"))
        );
    }

}
