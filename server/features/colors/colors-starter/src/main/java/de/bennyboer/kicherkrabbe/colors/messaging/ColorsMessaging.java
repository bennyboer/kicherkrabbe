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
                "colors.user-created-add-permission-to-create-colors",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

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
                "colors.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

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
                "colors.color-created-update-lookup",
                AggregateType.of("COLOR"),
                EventName.of("CREATED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

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
                "colors.color-updated-update-lookup",
                AggregateType.of("COLOR"),
                EventName.of("UPDATED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

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
                "colors.color-deleted-remove-from-lookup",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

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
                "colors.color-created-allow-creator-to-manage-color",
                AggregateType.of("COLOR"),
                EventName.of("CREATED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

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
                "colors.color-deleted-remove-permissions-for-color",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForColor(colorId);
                }
        );
    }

}
