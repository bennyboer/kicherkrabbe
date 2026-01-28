package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.fabrictypes.http.FabricTypesHttpConfig;
import de.bennyboer.kicherkrabbe.fabrictypes.messaging.FabricTypesMessaging;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.FabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.mongo.MongoFabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.fabrictypes.Actions.READ;

@Configuration
@Import({
        FabricTypesAggregateConfig.class,
        FabricTypesPermissionsConfig.class,
        FabricTypesHttpConfig.class,
        FabricTypesMessaging.class,
        SecurityConfig.class
})
public class FabricTypesModuleConfig {

    @Bean
    public FabricTypeLookupRepo fabricTypeLookupRepo(ReactiveMongoTemplate template) {
        return new MongoFabricTypeLookupRepo(template);
    }

    @Bean("fabricTypeChangesTracker")
    public ResourceChangesTracker fabricTypeChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("fabricTypesPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("FABRIC_TYPE"),
                READ,
                event -> {
                    var metadata = event.getMetadata();

                    return Map.of(
                            "aggregateType", metadata.getAggregateType().getValue(),
                            "aggregateId", metadata.getAggregateId().getValue(),
                            "aggregateVersion", metadata.getAggregateVersion().getValue(),
                            "event", event.getEvent()
                    );
                }
        );
    }

    @Bean
    public FabricTypesModule fabricTypesModule(
            FabricTypeService fabricTypeService,
            @Qualifier("fabricTypesPermissionsService") PermissionsService permissionsService,
            FabricTypeLookupRepo fabricTypeLookupRepo,
            @Qualifier("fabricTypeChangesTracker") ResourceChangesTracker fabricTypeChangesTracker
    ) {
        return new FabricTypesModule(
                fabricTypeService,
                permissionsService,
                fabricTypeLookupRepo,
                fabricTypeChangesTracker
        );
    }

}
