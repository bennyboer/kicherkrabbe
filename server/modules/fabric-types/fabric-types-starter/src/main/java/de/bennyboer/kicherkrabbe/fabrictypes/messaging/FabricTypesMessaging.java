package de.bennyboer.kicherkrabbe.fabrictypes.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class FabricTypesMessaging {

    @Bean
    public EventListener onUserCreatedAllowUserToCreateFabricTypes(
            EventListenerFactory factory,
            FabricTypesModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-allow-user-to-create-fabric-types",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateFabricTypes(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveFabricTypePermissionsForUser(
            EventListenerFactory factory,
            FabricTypesModule module
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
    public EventListener onFabricTypeCreatedUpdateLookup(
            EventListenerFactory factory,
            FabricTypesModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-created-update-lookup",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("CREATED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.updateFabricTypeInLookup(fabricTypeId);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeUpdatedUpdateLookup(
            EventListenerFactory factory,
            FabricTypesModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-updated-update-lookup",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("UPDATED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.updateFabricTypeInLookup(fabricTypeId);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeDeletedRemoveFabricTypeFromLookup(
            EventListenerFactory factory,
            FabricTypesModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabricType-deleted-remove-fabric-type-from-lookup",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.removeFabricTypeFromLookup(fabricTypeId);
                }
        );
    }

    @Bean
    public EventListener onFabricTypeCreatedAllowUserToManageFabricType(
            EventListenerFactory factory,
            FabricTypesModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-created-allow-user-to-manage-fabric-type",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("CREATED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    Agent agent = event.getMetadata().getAgent();
                    if (agent.getType() == AgentType.USER) {
                        String userId = agent.getId().getValue();
                        return module.allowUserToManageFabricType(fabricTypeId, userId);
                    }

                    return Mono.empty();
                }
        );
    }

    @Bean
    public EventListener onFabricTypeDeletedRemovePermissionsForFabricType(
            EventListenerFactory factory,
            FabricTypesModule module
    ) {
        return factory.createEventListenerForEvent(
                "fabric-type-deleted-remove-permissions",
                AggregateType.of("FABRIC_TYPE"),
                EventName.of("DELETED"),
                (event) -> {
                    String fabricTypeId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForFabricType(fabricTypeId);
                }
        );
    }

}
