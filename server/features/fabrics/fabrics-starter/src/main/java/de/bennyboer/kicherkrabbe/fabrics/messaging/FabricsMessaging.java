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

    @Bean
    public EventListener onUserCreatedAllowUserToCreateFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-allow-user-to-create-fabrics",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateFabrics(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveFabricsPermissionsForUser(
            EventListenerFactory factory,
            FabricsModule module
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
    public EventListener onFabricCreatedOrUpdatedUpdateLookup(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForAllEvents(
                "fabric-created-or-updated-update-lookup",
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

    @Bean
    public EventListener onFabricDeletedRemoveFromLookup(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-deleted-remove-from-lookup",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.removeFabricFromLookup(fabricId);
                }
        );
    }

    @Bean
    public EventListener onFabricCreatedAllowUserToManageFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-created-allow-user-to-manage-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManageFabric(fabricId, userId);
                }
        );
    }

    @Bean
    public EventListener onFabricDeletedRemovePermissionsOnFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-deleted-remove-permissions",
                AggregateType.of("FABRIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnFabric(fabricId);
                }
        );
    }

    @Bean
    public EventListener onFabricPublishedAllowAnonymousAndSystemUsersToReadPublishedFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-published-allow-anonymous-and-system-users-to-read-published-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("PUBLISHED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadPublishedFabric(fabricId);
                }
        );
    }

    @Bean
    public EventListener onFabricUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedFabric(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-unpublished-disallow-anonymous-and-system-users-to-read-published-fabric",
                AggregateType.of("FABRIC"),
                EventName.of("UNPUBLISHED"),
                (event) -> {
                    String fabricId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadPublishedFabric(fabricId);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeDeletedRemoveFabricTypeFromFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-deleted-remove-fabric-type-from-fabrics",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.removeFabricTypeFromFabrics(fabricTypeId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean
    public EventListener onTopicDeletedRemoveTopicFromFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-deleted-remove-topic-from-fabrics",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.removeTopicFromFabrics(topicId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean
    public EventListener onColorDeletedRemoveColorFromFabrics(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-deleted-remove-color-from-fabrics",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

                    return module.removeColorFromFabrics(colorId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean
    public EventListener onTopicCreatedMarkTopicAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-created-mark-topic-as-available",
                AggregateType.of("TOPIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();

                    return module.markTopicAsAvailable(topicId, name);
                }
        );
    }

    @Bean
    public EventListener onTopicUpdatedMarkTopicAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-updated-mark-topic-as-available",
                AggregateType.of("TOPIC"),
                EventName.of("UPDATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();

                    return module.markTopicAsAvailable(topicId, name);
                }
        );
    }

    @Bean
    public EventListener onTopicDeletedMarkTopicAsUnavailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-deleted-mark-topic-as-unavailable",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.markTopicAsUnavailable(topicId);
                }
        );
    }

    @Bean
    public EventListener onColorCreatedMarkColorAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-created-mark-color-as-available",
                AggregateType.of("COLOR"),
                EventName.of("CREATED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();
                    int red = (int) event.getEvent().get("red");
                    int green = (int) event.getEvent().get("green");
                    int blue = (int) event.getEvent().get("blue");

                    return module.markColorAsAvailable(colorId, name, red, green, blue);
                }
        );
    }

    @Bean
    public EventListener onColorUpdatedMarkColorAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-updated-mark-color-as-available",
                AggregateType.of("COLOR"),
                EventName.of("UPDATED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();
                    int red = (int) event.getEvent().get("red");
                    int green = (int) event.getEvent().get("green");
                    int blue = (int) event.getEvent().get("blue");

                    return module.markColorAsAvailable(colorId, name, red, green, blue);
                }
        );
    }

    @Bean
    public EventListener onColorDeletedMarkColorAsUnavailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "color-deleted-mark-color-as-unavailable",
                AggregateType.of("COLOR"),
                EventName.of("DELETED"),
                (event) -> {
                    String colorId = event.getMetadata().getAggregateId().getValue();

                    return module.markColorAsUnavailable(colorId);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeCreatedMarkFabricTypeAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-created-mark-fabric-type-as-available",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("CREATED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();

                    return module.markFabricTypeAsAvailable(fabricTypeId, name);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeUpdatedMarkFabricTypeAsAvailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-updated-mark-fabric-type-as-available",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("UPDATED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();

                    return module.markFabricTypeAsAvailable(fabricTypeId, name);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeDeletedMarkFabricTypeAsUnavailable(
            EventListenerFactory factory,
            FabricsModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-deleted-mark-fabric-type-as-unavailable",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.markFabricTypeAsUnavailable(fabricTypeId);
                }
        );
    }

}
