package de.bennyboer.kicherkrabbe.patterns.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.patterns.PatternsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class PatternsMessaging {

    @Bean
    public EventListener onUserCreatedAllowUserToCreatePatterns(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.user-created-allow-user-to-create-patterns",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreatePatterns(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemovePatternsPermissionsForUser(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean
    public EventListener onPatternCreatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-created-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("CREATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternRenamedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-renamed-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("RENAMED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternAttributionUpdatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-attribution-updated-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("ATTRIBUTION_UPDATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternCategoriesUpdatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-categories-updated-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("CATEGORIES_UPDATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternImagesUpdatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-images-updated-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("IMAGES_UPDATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternVariantsUpdatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-variants-updated-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("VARIANTS_UPDATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternExtrasUpdatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-extras-updated-update-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("EXTRAS_UPDATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternDeletedRemovePatternFromLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-deleted-remove-pattern-from-lookup",
                AggregateType.of("PATTERN"),
                EventName.of("DELETED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.removePatternFromLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternCreatedAllowUserToManagePattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-created-allow-user-to-manage-pattern",
                AggregateType.of("PATTERN"),
                EventName.of("CREATED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    Agent agent = event.getMetadata().getAgent();
                    if (agent.getType() == AgentType.USER) {
                        String userId = agent.getId().getValue();
                        return module.allowUserToManagePattern(patternId, userId);
                    }

                    return Mono.empty();
                }
        );
    }

    @Bean
    public EventListener onPatternDeletedRemovePermissionsForPattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-deleted-remove-permissions",
                AggregateType.of("PATTERN"),
                EventName.of("DELETED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForPattern(patternId);
                }
        );
    }

}
