package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.fabrics.http.FabricsHttpConfig;
import de.bennyboer.kicherkrabbe.fabrics.messaging.FabricsMessaging;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.mongo.MongoFabricLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.fabrics.Actions.READ;

@Configuration
@Import({
        FabricsAggregateConfig.class,
        FabricsPermissionsConfig.class,
        FabricsHttpConfig.class,
        FabricsMessaging.class,
        SecurityConfig.class
})
public class FabricsModuleConfig {

    @Bean
    public FabricLookupRepo fabricLookupRepo(ReactiveMongoTemplate template) {
        return new MongoFabricLookupRepo(template);
    }

    @Bean("fabricChangesTracker")
    public ResourceChangesTracker fabricChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("fabricsPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("FABRIC"),
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
    public FabricsModule fabricsModule(
            FabricService fabricService,
            @Qualifier("fabricsPermissionsService") PermissionsService permissionsService,
            FabricLookupRepo fabricLookupRepo,
            @Qualifier("fabricChangesTracker") ResourceChangesTracker fabricChangesTracker
    ) {
        return new FabricsModule(
                fabricService,
                permissionsService,
                fabricLookupRepo,
                fabricChangesTracker
        );
    }

}
