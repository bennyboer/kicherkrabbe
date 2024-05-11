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

}
