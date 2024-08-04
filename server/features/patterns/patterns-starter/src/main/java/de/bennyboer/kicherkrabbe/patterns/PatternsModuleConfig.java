package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.patterns.messaging.PatternsMessaging;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.PatternCategoryRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.mongo.MongoPatternCategoryRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo.MongoPatternLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.patterns.Actions.READ;

@Configuration
@Import({
        PatternsAggregateConfig.class,
        PatternsPermissionsConfig.class,
        PatternsMessaging.class,
        SecurityConfig.class
})
public class PatternsModuleConfig {

    @Bean
    public PatternLookupRepo patternLookupRepo(ReactiveMongoTemplate template) {
        return new MongoPatternLookupRepo(template);
    }

    @Bean
    public PatternCategoryRepo patternCategoryRepo(ReactiveMongoTemplate template) {
        return new MongoPatternCategoryRepo(template);
    }

    @Bean("patternChangesTracker")
    public ResourceChangesTracker patternChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("patternsPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("PATTERN"),
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
    public PatternsModule patternsModule(
            PatternService patternService,
            @Qualifier("patternsPermissionsService") PermissionsService permissionsService,
            PatternLookupRepo patternLookupRepo,
            @Qualifier("patternChangesTracker") ResourceChangesTracker patternChangesTracker,
            PatternCategoryRepo patternCategoryRepo
    ) {
        return new PatternsModule(
                patternService,
                permissionsService,
                patternLookupRepo,
                patternChangesTracker,
                patternCategoryRepo
        );
    }

}
