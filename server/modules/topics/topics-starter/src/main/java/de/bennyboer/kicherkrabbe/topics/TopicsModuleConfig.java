package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.changes.MessagingResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.changes.ResourceType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import de.bennyboer.kicherkrabbe.topics.http.TopicsHttpConfig;
import de.bennyboer.kicherkrabbe.topics.messaging.TopicsMessaging;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.TopicLookupRepo;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.mongo.MongoTopicLookupRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.topics.Actions.READ;

@Configuration
@Import({
        TopicsAggregateConfig.class,
        TopicsPermissionsConfig.class,
        TopicsHttpConfig.class,
        TopicsMessaging.class,
        SecurityConfig.class
})
public class TopicsModuleConfig {

    @Bean
    public TopicLookupRepo topicLookupRepo(ReactiveMongoTemplate template) {
        return new MongoTopicLookupRepo(template);
    }

    @Bean("topicChangesTracker")
    public ResourceChangesTracker topicChangesTracker(
            EventListenerFactory eventListenerFactory,
            PermissionEventListenerFactory permissionEventListenerFactory,
            @Qualifier("topicsPermissionsService") PermissionsService permissionsService
    ) {
        return new MessagingResourceChangesTracker(
                eventListenerFactory,
                permissionEventListenerFactory,
                permissionsService,
                ResourceType.of("TOPIC"),
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
    public TopicsModule topicsModule(
            TopicService topicService,
            @Qualifier("topicsPermissionsService") PermissionsService permissionsService,
            TopicLookupRepo topicLookupRepo,
            @Qualifier("topicChangesTracker") ResourceChangesTracker topicChangesTracker
    ) {
        return new TopicsModule(topicService, permissionsService, topicLookupRepo, topicChangesTracker);
    }

}
