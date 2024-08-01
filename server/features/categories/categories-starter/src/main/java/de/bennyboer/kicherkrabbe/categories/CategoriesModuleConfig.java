package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.categories.http.CategoriesHttpConfig;
import de.bennyboer.kicherkrabbe.categories.messaging.CategoriesMessaging;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.CategoryLookupRepo;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.mongo.MongoCategoryLookupRepo;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.categories.Actions.READ;

@Configuration
@Import({
        CategoriesAggregateConfig.class,
        CategoriesPermissionsConfig.class,
        CategoriesHttpConfig.class,
        CategoriesMessaging.class,
        SecurityConfig.class
})
public class CategoriesModuleConfig {

    @Bean
    public CategoryLookupRepo categoryLookupRepo(ReactiveMongoTemplate template) {
        return new MongoCategoryLookupRepo(template);
    }

    @Bean("categoryChangesTracker")
    public ResourceChangesTracker categoryChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("categoriesPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("CATEGORY"),
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
    public CategoriesModule categoriesModule(
            CategoryService categoryService,
            @Qualifier("categoriesPermissionsService") PermissionsService permissionsService,
            CategoryLookupRepo categoryLookupRepo,
            @Qualifier("categoryChangesTracker") ResourceChangesTracker categoryChangesTracker
    ) {
        return new CategoriesModule(categoryService, permissionsService, categoryLookupRepo, categoryChangesTracker);
    }

}
