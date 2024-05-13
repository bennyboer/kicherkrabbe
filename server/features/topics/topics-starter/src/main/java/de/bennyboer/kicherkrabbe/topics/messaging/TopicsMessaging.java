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

    @Bean
    public EventListener onUserCreatedAllowUserToCreateTopics(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "user-created-allow-user-to-create-topics",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateTopics(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveTopicsPermissionsForUser(
            EventListenerFactory factory,
            TopicsModule module
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
    public EventListener onTopicCreatedUpdateLookup(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-created-update-lookup",
                AggregateType.of("TOPIC"),
                EventName.of("CREATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.updateTopicInLookup(topicId);
                }
        );
    }

    @Bean
    public EventListener onTopicUpdatedUpdateLookup(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-updated-update-lookup",
                AggregateType.of("TOPIC"),
                EventName.of("UPDATED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.updateTopicInLookup(topicId);
                }
        );
    }

    @Bean
    public EventListener onTopicDeletedRemoveTopicFromLookup(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-deleted-remove-topic-from-lookup",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.removeTopicFromLookup(topicId);
                }
        );
    }

    @Bean
    public EventListener onTopicCreatedAllowUserToManageTopic(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-created-allow-user-to-manage-topic",
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

    @Bean
    public EventListener onTopicDeletedRemovePermissionsForTopic(
            EventListenerFactory factory,
            TopicsModule module
    ) {
        return factory.createEventListenerForEvent(
                "topic-deleted-remove-permissions",
                AggregateType.of("TOPIC"),
                EventName.of("DELETED"),
                (event) -> {
                    String topicId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForTopic(topicId);
                }
        );
    }

}
