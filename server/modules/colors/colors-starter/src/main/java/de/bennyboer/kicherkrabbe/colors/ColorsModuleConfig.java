package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.colors.http.ColorsHttpConfig;
import de.bennyboer.kicherkrabbe.colors.messaging.ColorsMessaging;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.mongo.MongoColorLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.colors.Actions.READ;

@Configuration
@Import({
        ColorsAggregateConfig.class,
        ColorsPermissionsConfig.class,
        ColorsHttpConfig.class,
        ColorsMessaging.class,
        SecurityConfig.class
})
public class ColorsModuleConfig {

    @Bean
    public ColorLookupRepo colorLookupRepo(ReactiveMongoTemplate template) {
        return new MongoColorLookupRepo(template);
    }

    @Bean("colorChangesTracker")
    public ResourceChangesTracker colorChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("colorsPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("COLOR"),
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
    public ColorsModule colorsModule(
            ColorService colorService,
            @Qualifier("colorsPermissionsService") PermissionsService permissionsService,
            ColorLookupRepo colorLookupRepo,
            @Qualifier("colorChangesTracker") ResourceChangesTracker colorChangesTracker
    ) {
        return new ColorsModule(colorService, permissionsService, colorLookupRepo, colorChangesTracker);
    }

}
