package de.bennyboer.kicherkrabbe.assets.messaging;

import de.bennyboer.kicherkrabbe.assets.AssetsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetsMessaging {

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

}
