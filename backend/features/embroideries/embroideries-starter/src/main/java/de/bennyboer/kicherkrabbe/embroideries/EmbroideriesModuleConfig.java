package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.EmbroideryCategoryRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.mongo.MongoEmbroideryCategoryRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.EmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.mongo.MongoEmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.embroideries.Actions.READ;

@Configuration
@Import({
        EmbroideriesAggregateConfig.class,
        EmbroideriesPermissionsConfig.class
})
public class EmbroideriesModuleConfig {

    @Bean
    public EmbroideryLookupRepo embroideryLookupRepo(ReactiveMongoTemplate template) {
        return new MongoEmbroideryLookupRepo(template);
    }

    @Bean
    public EmbroideryCategoryRepo embroideryCategoryRepo(ReactiveMongoTemplate template) {
        return new MongoEmbroideryCategoryRepo(template);
    }

    @Bean("embroideryChangesTracker")
    public ResourceChangesTracker embroideryChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("embroideriesPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("EMBROIDERY"),
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
    public EmbroideriesModule embroideriesModule(
            EmbroideryService embroideryService,
            @Qualifier("embroideriesPermissionsService") PermissionsService permissionsService,
            EmbroideryLookupRepo embroideryLookupRepo,
            @Qualifier("embroideryChangesTracker") ResourceChangesTracker embroideryChangesTracker,
            EmbroideryCategoryRepo embroideryCategoryRepo
    ) {
        return new EmbroideriesModule(
                embroideryService,
                permissionsService,
                embroideryLookupRepo,
                embroideryChangesTracker,
                embroideryCategoryRepo
        );
    }

}
