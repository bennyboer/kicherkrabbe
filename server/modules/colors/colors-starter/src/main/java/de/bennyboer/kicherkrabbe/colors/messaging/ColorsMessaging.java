package de.bennyboer.kicherkrabbe.colors.messaging;

import de.bennyboer.kicherkrabbe.colors.ColorsModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ColorsMessaging {

    @Bean
    public EventListener onUserCreatedAddPermissionToCreateColorsMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-add-permission-to-create-colors",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();

                    return module.allowUserToCreateColors(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveColorPermissionsMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (metadata, version, payload) -> {
                    String userId = metadata.getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean
    public EventListener onColorCreatedUpdateLookupMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-created-update-lookup",
                AggregateType.of("COLOR"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String colorId = metadata.getAggregateId().getValue();

                    return module.updateColorInLookup(colorId);
                }
        );
    }

    @Bean
    public EventListener onColorUpdatedUpdateLookupMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-updated-update-lookup",
                AggregateType.of("COLOR"),
                EventName.of("UPDATED"),
                (metadata, version, payload) -> {
                    String colorId = metadata.getAggregateId().getValue();

                    return module.updateColorInLookup(colorId);
                }
        );
    }

    @Bean
    public EventListener onColorDeletedRemoveFromLookupMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-deleted-remove-from-lookup",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (metadata, version, payload) -> {
                    String colorId = metadata.getAggregateId().getValue();

                    return module.removeColorFromLookup(colorId);
                }
        );
    }

    @Bean
    public EventListener onColorCreatedAllowCreatorToManageColorMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-created-allow-creator-to-manage-color",
                AggregateType.of("COLOR"),
                EventName.of("CREATED"),
                (metadata, version, payload) -> {
                    String colorId = metadata.getAggregateId().getValue();
                    String userId = metadata.getAgent().getId().getValue();

                    return module.allowCreatorToManageColor(colorId, userId);
                }
        );
    }

    @Bean
    public EventListener onColorDeletedRemovePermissionsForColorMsgListener(
            EventListenerFactory factory,
            ColorsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-deleted-remove-permissions-for-color",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (metadata, version, payload) -> {
                    String colorId = metadata.getAggregateId().getValue();

                    return module.removePermissionsForColor(colorId);
                }
        );
    }

}
