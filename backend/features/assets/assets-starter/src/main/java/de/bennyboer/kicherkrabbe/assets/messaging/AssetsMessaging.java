package de.bennyboer.kicherkrabbe.assets.messaging;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.AssetsModule;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetResourceId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AssetsMessaging {

    record FabricCreatedEvent(String image) {
    }

    record FabricImageUpdatedEvent(String image) {
    }

    record PatternCreatedEvent(List<String> images) {
    }

    record PatternImagesUpdatedEvent(List<String> images) {
    }

    record ProductCreatedEvent(List<String> images) {
    }

    record ProductImagesUpdatedEvent(List<String> images) {
    }

    record HighlightCreatedEvent(String imageId) {
    }

    record HighlightImageUpdatedEvent(String imageId) {
    }

    @Bean("assets_onUserCreatedAllowUserToCreateAssets")
    public EventListener onUserCreatedAllowUserToCreateAssets(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.user-created-allow-user-to-create-assets",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateAssets(userId);
                }
        );
    }

    @Bean("assets_onUserDeletedRemoveAssetsPermissionsForUser")
    public EventListener onUserDeletedRemoveAssetsPermissionsForUser(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("assets_onAssetCreatedAllowUserToManageAsset")
    public EventListener onAssetCreatedAllowUserToManageAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.asset-created-allow-user-to-manage-asset",
                AggregateType.of("ASSET"),
                EventName.of("CREATED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManageAsset(assetId, userId);
                }
        );
    }

    @Bean("assets_onAssetDeletedRemovePermissionsForAsset")
    public EventListener onAssetDeletedRemovePermissionsForAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.asset-deleted-remove-permissions",
                AggregateType.of("ASSET"),
                EventName.of("DELETED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnAsset(assetId);
                }
        );
    }

    @Bean("assets_onAssetCreatedAllowAnonymousUsersToReadAsset")
    public EventListener onAssetCreatedAllowAnonymousUsersToReadAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.asset-created-allow-anonymous-users-to-read-asset",
                AggregateType.of("ASSET"),
                EventName.of("CREATED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousUsersToReadAsset(assetId);
                }
        );
    }

    @Bean("assets_onAssetDeletedDisallowAnonymousUsersToReadAsset")
    public EventListener onAssetDeletedDisallowAnonymousUsersToReadAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.asset-deleted-disallow-anonymous-users-to-read-asset",
                AggregateType.of("ASSET"),
                EventName.of("DELETED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousUsersToReadAsset(assetId);
                }
        );
    }

    @Bean("assets_onFabricCreatedUpdateAssetReferences")
    public EventListener onFabricCreatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.fabric-created-update-asset-references",
                AggregateType.of("FABRIC"),
                EventName.of("CREATED"),
                FabricCreatedEvent.class,
                (metadata, event) -> {
                    String fabricId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.FABRIC,
                            AssetResourceId.of(fabricId),
                            Set.of(AssetId.of(event.image()))
                    );
                }
        );
    }

    @Bean("assets_onFabricImageUpdatedUpdateAssetReferences")
    public EventListener onFabricImageUpdatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.fabric-image-updated-update-asset-references",
                AggregateType.of("FABRIC"),
                EventName.of("IMAGE_UPDATED"),
                FabricImageUpdatedEvent.class,
                (metadata, event) -> {
                    String fabricId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.FABRIC,
                            AssetResourceId.of(fabricId),
                            Set.of(AssetId.of(event.image()))
                    );
                }
        );
    }

    @Bean("assets_onFabricDeletedRemoveAssetReferences")
    public EventListener onFabricDeletedRemoveAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.fabric-deleted-remove-asset-references",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.removeAssetReferencesByResource(
                            AssetReferenceResourceType.FABRIC,
                            AssetResourceId.of(fabricId)
                    );
                }
        );
    }

    @Bean("assets_onPatternCreatedUpdateAssetReferences")
    public EventListener onPatternCreatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.pattern-created-update-asset-references",
                AggregateType.of("PATTERN"),
                EventName.of("CREATED"),
                PatternCreatedEvent.class,
                (metadata, event) -> {
                    String patternId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.PATTERN,
                            AssetResourceId.of(patternId),
                            toAssetIds(event.images())
                    );
                }
        );
    }

    @Bean("assets_onPatternImagesUpdatedUpdateAssetReferences")
    public EventListener onPatternImagesUpdatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.pattern-images-updated-update-asset-references",
                AggregateType.of("PATTERN"),
                EventName.of("IMAGES_UPDATED"),
                PatternImagesUpdatedEvent.class,
                (metadata, event) -> {
                    String patternId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.PATTERN,
                            AssetResourceId.of(patternId),
                            toAssetIds(event.images())
                    );
                }
        );
    }

    @Bean("assets_onPatternDeletedRemoveAssetReferences")
    public EventListener onPatternDeletedRemoveAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.pattern-deleted-remove-asset-references",
                AggregateType.of("PATTERN"),
                EventName.of("DELETED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.removeAssetReferencesByResource(
                            AssetReferenceResourceType.PATTERN,
                            AssetResourceId.of(patternId)
                    );
                }
        );
    }

    @Bean("assets_onProductCreatedUpdateAssetReferences")
    public EventListener onProductCreatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.product-created-update-asset-references",
                AggregateType.of("PRODUCT"),
                EventName.of("CREATED"),
                ProductCreatedEvent.class,
                (metadata, event) -> {
                    String productId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.PRODUCT,
                            AssetResourceId.of(productId),
                            toAssetIds(event.images())
                    );
                }
        );
    }

    @Bean("assets_onProductImagesUpdatedUpdateAssetReferences")
    public EventListener onProductImagesUpdatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.product-images-updated-update-asset-references",
                AggregateType.of("PRODUCT"),
                EventName.of("IMAGES_UPDATED"),
                ProductImagesUpdatedEvent.class,
                (metadata, event) -> {
                    String productId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.PRODUCT,
                            AssetResourceId.of(productId),
                            toAssetIds(event.images())
                    );
                }
        );
    }

    @Bean("assets_onProductDeletedRemoveAssetReferences")
    public EventListener onProductDeletedRemoveAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.product-deleted-remove-asset-references",
                AggregateType.of("PRODUCT"),
                EventName.of("DELETED"),
                (event) -> {
                    String productId = event.getMetadata().getAggregateId().getValue();

                    return module.removeAssetReferencesByResource(
                            AssetReferenceResourceType.PRODUCT,
                            AssetResourceId.of(productId)
                    );
                }
        );
    }

    @Bean("assets_onHighlightCreatedUpdateAssetReferences")
    public EventListener onHighlightCreatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.highlight-created-update-asset-references",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("CREATED"),
                HighlightCreatedEvent.class,
                (metadata, event) -> {
                    String highlightId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.HIGHLIGHT,
                            AssetResourceId.of(highlightId),
                            Set.of(AssetId.of(event.imageId()))
                    );
                }
        );
    }

    @Bean("assets_onHighlightImageUpdatedUpdateAssetReferences")
    public EventListener onHighlightImageUpdatedUpdateAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.highlight-image-updated-update-asset-references",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("IMAGE_UPDATED"),
                HighlightImageUpdatedEvent.class,
                (metadata, event) -> {
                    String highlightId = metadata.getAggregateId().getValue();

                    return module.updateAssetReferences(
                            AssetReferenceResourceType.HIGHLIGHT,
                            AssetResourceId.of(highlightId),
                            Set.of(AssetId.of(event.imageId()))
                    );
                }
        );
    }

    @Bean("assets_onHighlightDeletedRemoveAssetReferences")
    public EventListener onHighlightDeletedRemoveAssetReferences(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "assets.highlight-deleted-remove-asset-references",
                AggregateType.of("HIGHLIGHT"),
                EventName.of("DELETED"),
                (event) -> {
                    String highlightId = event.getMetadata().getAggregateId().getValue();

                    return module.removeAssetReferencesByResource(
                            AssetReferenceResourceType.HIGHLIGHT,
                            AssetResourceId.of(highlightId)
                    );
                }
        );
    }

    private static Set<AssetId> toAssetIds(List<String> ids) {
        return ids.stream()
                .map(AssetId::of)
                .collect(Collectors.toSet());
    }

}
