package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.highlights.http.HighlightsHttpConfig;
import de.bennyboer.kicherkrabbe.highlights.messaging.HighlightsMessaging;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.HighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.mongo.MongoLinkLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.mongo.MongoHighlightLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.ReactiveTransactionManager;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.highlights.Actions.READ;

@Configuration
@Import({
        HighlightsAggregateConfig.class,
        HighlightsPermissionsConfig.class,
        HighlightsHttpConfig.class,
        HighlightsMessaging.class,
        SecurityConfig.class
})
public class HighlightsModuleConfig {

    @Bean
    public HighlightLookupRepo highlightLookupRepo(ReactiveMongoTemplate template) {
        return new MongoHighlightLookupRepo(template);
    }

    @Bean
    public LinkLookupRepo linkLookupRepo(ReactiveMongoTemplate template) {
        return new MongoLinkLookupRepo(template);
    }

    @Bean("highlightChangesTracker")
    public ResourceChangesTracker highlightChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("highlightsPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("HIGHLIGHT"),
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
    public HighlightsModule highlightsModule(
            HighlightService highlightService,
            @Qualifier("highlightsPermissionsService") PermissionsService permissionsService,
            HighlightLookupRepo highlightLookupRepo,
            LinkLookupRepo linkLookupRepo,
            @Qualifier("highlightChangesTracker") ResourceChangesTracker highlightChangesTracker,
            ReactiveTransactionManager transactionManager
    ) {
        return new HighlightsModule(
                highlightService,
                permissionsService,
                highlightLookupRepo,
                linkLookupRepo,
                highlightChangesTracker,
                transactionManager
        );
    }

}
