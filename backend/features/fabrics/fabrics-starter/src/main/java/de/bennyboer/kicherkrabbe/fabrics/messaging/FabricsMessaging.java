package de.bennyboer.kicherkrabbe.fabrics.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.fabrics.FabricsModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class FabricsMessaging {

    record TopicEvent(String name) {
    }

    record ColorEvent(String name, int red, int green, int blue) {
    }

    record FabricTypeEvent(String name) {
    }

    @Bean("fabrics_onUserCreatedAllowUserToCreateFabrics")
    public EventListener onUserCreatedAllowUserToCreateFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.user-created-allow-user-to-create-fabrics",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateFabrics(userId);
                }
        );
    }

    @Bean("fabrics_onUserDeletedRemoveFabricsPermissionsForUser")
    public EventListener onUserDeletedRemoveFabricsPermissionsForUser(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("fabrics_onFabricCreatedOrUpdatedUpdateLookup")
    public EventListener onFabricCreatedOrUpdatedUpdateLookup(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForAllEvents(
                "fabrics.fabric-created-or-updated-update-lookup",
                AggregateType.of("FABRIC"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();
                    boolean isDeleted = event.getEventName().equals(EventName.of("DELETED"));
                    if (isDeleted) {
                        return Mono.empty();
                    }

                    return module.updateFabricInLookup(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricDeletedRemoveFromLookup")
    public EventListener onFabricDeletedRemoveFromLookup(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-deleted-remove-from-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.removeFabricFromLookup(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricCreatedAllowUserToManageFabric")
    public EventListener onFabricCreatedAllowUserToManageFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-created-allow-user-to-manage-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManageFabric(fabricId, userId);
                }
        );
    }

    @Bean("fabrics_onFabricDeletedRemovePermissionsOnFabric")
    public EventListener onFabricDeletedRemovePermissionsOnFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-deleted-remove-permissions",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnFabric(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricPublishedAllowAnonymousAndSystemUsersToReadPublishedFabric")
    public EventListener onFabricPublishedAllowAnonymousAndSystemUsersToReadPublishedFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-published-allow-anonymous-and-system-users-to-read-published-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("PUBLISHED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadPublishedFabric(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedFabric")
    public EventListener onFabricUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-unpublished-disallow-anonymous-and-system-users-to-read-published-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("UNPUBLISHED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadPublishedFabric(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricFeaturedAllowAnonymousAndSystemUsersToReadFeaturedFabric")
    public EventListener onFabricFeaturedAllowAnonymousAndSystemUsersToReadFeaturedFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-featured-allow-anonymous-and-system-users-to-read-featured-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("FEATURED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadFeaturedFabric(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricUnfeaturedDisallowAnonymousAndSystemUsersToReadFeaturedFabric")
    public EventListener onFabricUnfeaturedDisallowAnonymousAndSystemUsersToReadFeaturedFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-unfeatured-disallow-anonymous-and-system-users-to-read-featured-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("UNFEATURED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadFeaturedFabric(fabricId);
                }
        );
    }

    @Bean("fabrics_onFabricTypeDeletedRemoveFabricTypeFromFabrics")
    public EventListener onFabricTypeDeletedRemoveFabricTypeFromFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-type-deleted-remove-fabric-type-from-fabrics",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.removeFabricTypeFromFabrics(fabricTypeId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean("fabrics_onTopicDeletedRemoveTopicFromFabrics")
    public EventListener onTopicDeletedRemoveTopicFromFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.topic-deleted-remove-topic-from-fabrics",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.removeTopicFromFabrics(topicId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean("fabrics_onColorDeletedRemoveColorFromFabrics")
    public EventListener onColorDeletedRemoveColorFromFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.color-deleted-remove-color-from-fabrics",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

                    return module.removeColorFromFabrics(colorId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean("fabrics_onTopicCreatedMarkTopicAsAvailable")
    public EventListener onTopicCreatedMarkTopicAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.topic-created-mark-topic-as-available",
                AggregateType.of("TOPIC"),
                EventName.of("CREATED"),
                TopicEvent.class,
                (metadata, event) -> {
                    String topicId = metadata.getAggregateId().getValue();

                    return module.markTopicAsAvailable(topicId, event.name());
                }
        );
    }

    @Bean("fabrics_onTopicUpdatedMarkTopicAsAvailable")
    public EventListener onTopicUpdatedMarkTopicAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.topic-updated-mark-topic-as-available",
                AggregateType.of("TOPIC"),
                EventName.of("UPDATED"),
                TopicEvent.class,
                (metadata, event) -> {
                    String topicId = metadata.getAggregateId().getValue();

                    return module.markTopicAsAvailable(topicId, event.name());
                }
        );
    }

    @Bean("fabrics_onTopicDeletedMarkTopicAsUnavailable")
    public EventListener onTopicDeletedMarkTopicAsUnavailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.topic-deleted-mark-topic-as-unavailable",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.markTopicAsUnavailable(topicId);
                }
        );
    }

    @Bean("fabrics_onColorCreatedMarkColorAsAvailable")
    public EventListener onColorCreatedMarkColorAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.color-created-mark-color-as-available",
                AggregateType.of("COLOR"),
                EventName.of("CREATED"),
                ColorEvent.class,
                (metadata, event) -> {
                    String colorId = metadata.getAggregateId().getValue();

                    return module.markColorAsAvailable(colorId, event.name(), event.red(), event.green(), event.blue());
                }
        );
    }

    @Bean("fabrics_onColorUpdatedMarkColorAsAvailable")
    public EventListener onColorUpdatedMarkColorAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.color-updated-mark-color-as-available",
                AggregateType.of("COLOR"),
                EventName.of("UPDATED"),
                ColorEvent.class,
                (metadata, event) -> {
                    String colorId = metadata.getAggregateId().getValue();

                    return module.markColorAsAvailable(colorId, event.name(), event.red(), event.green(), event.blue());
                }
        );
    }

    @Bean("fabrics_onColorDeletedMarkColorAsUnavailable")
    public EventListener onColorDeletedMarkColorAsUnavailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.color-deleted-mark-color-as-unavailable",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

                    return module.markColorAsUnavailable(colorId);
                }
        );
    }

    @Bean("fabrics_onFabricTypeCreatedMarkFabricTypeAsAvailable")
    public EventListener onFabricTypeCreatedMarkFabricTypeAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-type-created-mark-fabric-type-as-available",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("CREATED"),
                FabricTypeEvent.class,
                (metadata, event) -> {
                    String fabricTypeId = metadata.getAggregateId().getValue();

                    return module.markFabricTypeAsAvailable(fabricTypeId, event.name());
                }
        );
    }

    @Bean("fabrics_onFabricTypeUpdatedMarkFabricTypeAsAvailable")
    public EventListener onFabricTypeUpdatedMarkFabricTypeAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-type-updated-mark-fabric-type-as-available",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("UPDATED"),
                FabricTypeEvent.class,
                (metadata, event) -> {
                    String fabricTypeId = metadata.getAggregateId().getValue();

                    return module.markFabricTypeAsAvailable(fabricTypeId, event.name());
                }
        );
    }

    @Bean("fabrics_onFabricTypeDeletedMarkFabricTypeAsUnavailable")
    public EventListener onFabricTypeDeletedMarkFabricTypeAsUnavailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabrics.fabric-type-deleted-mark-fabric-type-as-unavailable",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.markFabricTypeAsUnavailable(fabricTypeId);
                }
        );
    }

}
