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

    @Bean
    public EventListener onUserCreatedAllowUserToCreateAssets(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-allow-user-to-create-assets",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateAssets(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveAssetsPermissionsForUser(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean
    public EventListener onAssetCreatedAllowUserToManageAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "asset-created-allow-user-to-manage-asset",
                AggregateType.of("ASSET"),
                EventName.of("CREATED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManageAsset(assetId, userId);
                }
        );
    }

    @Bean
    public EventListener onAssetDeletedRemovePermissionsForAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "asset-deleted-remove-permissions",
                AggregateType.of("ASSET"),
                EventName.of("DELETED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnAsset(assetId);
                }
        );
    }

    @Bean
    public EventListener onAssetCreatedAllowAnonymousUsersToReadAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "asset-created-allow-anonymous-users-to-read-asset",
                AggregateType.of("ASSET"),
                EventName.of("CREATED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousUsersToReadAsset(assetId);
                }
        );
    }

    @Bean
    public EventListener onAssetDeletedDisallowAnonymousUsersToReadAsset(
            EventListenerFactory factory,
            AssetsModule module
    ) {
        return factory.createEventListenerForEvent(
                "asset-deleted-disallow-anonymous-users-to-read-asset",
                AggregateType.of("ASSET"),
                EventName.of("DELETED"),
                (event) -> {
                    String assetId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousUsersToReadAsset(assetId);
                }
        );
    }

}
