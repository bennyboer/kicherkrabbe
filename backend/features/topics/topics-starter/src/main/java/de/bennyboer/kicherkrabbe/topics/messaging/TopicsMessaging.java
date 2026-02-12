package de.bennyboer.kicherkrabbe.topics.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.topics.TopicsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class TopicsMessaging {

    @Bean("topics_onUserCreatedAllowUserToCreateTopics")
    public EventListener onUserCreatedAllowUserToCreateTopics(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.user-created-allow-user-to-create-topics",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateTopics(userId);
                }
        );
    }

    @Bean("topics_onUserDeletedRemoveTopicsPermissionsForUser")
    public EventListener onUserDeletedRemoveTopicsPermissionsForUser(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("topics_onTopicCreatedUpdateLookup")
    public EventListener onTopicCreatedUpdateLookup(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.topic-created-update-lookup",
                AggregateType.of("TOPIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.updateTopicInLookup(topicId);
                }
        );
    }

    @Bean("topics_onTopicUpdatedUpdateLookup")
    public EventListener onTopicUpdatedUpdateLookup(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.topic-updated-update-lookup",
                AggregateType.of("TOPIC"),
                EventName.of("UPDATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.updateTopicInLookup(topicId);
                }
        );
    }

    @Bean("topics_onTopicDeletedRemoveTopicFromLookup")
    public EventListener onTopicDeletedRemoveTopicFromLookup(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.topic-deleted-remove-topic-from-lookup",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.removeTopicFromLookup(topicId);
                }
        );
    }

    @Bean("topics_onTopicCreatedAllowUserToManageTopic")
    public EventListener onTopicCreatedAllowUserToManageTopic(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.topic-created-allow-user-to-manage-topic",
                AggregateType.of("TOPIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    Agent agent = event.getMetadata().getAgent();
                    if (agent.getType() == AgentType.USER) {
                        String userId = agent.getId().getValue();
                        return module.allowUserToManageTopic(topicId, userId);
                    }

                    return Mono.empty();
                }
        );
    }

    @Bean("topics_onTopicDeletedRemovePermissionsForTopic")
    public EventListener onTopicDeletedRemovePermissionsForTopic(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topics.topic-deleted-remove-permissions",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForTopic(topicId);
                }
        );
    }

}
