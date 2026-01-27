package de.bennyboer.kicherkrabbe.patterns.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
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
    public EventListener onPatternCreatedOrUpdatedUpdateLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForAllEvents(
                "patterns.pattern-created-or-updated-update-lookup",
                AggregateType.of("PATTERN"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();
                    boolean isDeleted = event.getEventName().equals(EventName.of("DELETED"));
                    if (isDeleted) {
                        return Mono.empty();
                    }

                    return module.updatePatternInLookup(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternDeletedRemoveFromLookup(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-deleted-remove-from-lookup",
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
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManagePattern(patternId, userId);
                }
        );
    }

    @Bean
    public EventListener onPatternDeletedRemovePermissionsOnPattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-deleted-remove-permissions",
                AggregateType.of("PATTERN"),
                EventName.of("DELETED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnPattern(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternPublishedAllowAnonymousAndSystemUsersToReadPublishedPattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-published-allow-anonymous-and-system-users-to-read-published-pattern",
                AggregateType.of("PATTERN"),
                EventName.of("PUBLISHED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadPublishedPattern(patternId);
                }
        );
    }

    @Bean
    public EventListener onPatternUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedPattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-unpublished-disallow-anonymous-and-system-users-to-read-published-pattern",
                AggregateType.of("PATTERN"),
                EventName.of("UNPUBLISHED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadPublishedPattern(patternId);
                }
        );
    }

    @Bean
    public EventListener onCategoryDeletedRemoveCategoryFromPatterns(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-deleted-remove-category-from-patterns",
                AggregateType.of("CATEGORY"),
                EventName.of("DELETED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.removeCategoryFromPatterns(categoryId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean
    public EventListener onCategoryCreatedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-created-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("CREATED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();

                    return module.markCategoryAsAvailable(categoryId, name);
                }
        );
    }

    @Bean
    public EventListener onCategoryRenamedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-renamed-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("RENAMED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();

                    return module.markCategoryAsAvailable(categoryId, name);
                }
        );
    }

    @Bean
    public EventListener onCategoryRegroupedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-regrouped-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("REGROUPED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();
                    String name = event.getEvent().get("name").toString();
                    String group = event.getEvent().get("group").toString();
                    if (!group.equals("CLOTHING")) {
                        return Mono.empty();
                    }

                    return module.markCategoryAsAvailable(categoryId, name);
                }
        );
    }

    @Bean
    public EventListener onCategoryRegroupedToNonClothingCategoryMarkCategoryAsUnavailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-regrouped-to-non-clothing-mark-category-as-unavailable",
                AggregateType.of("CATEGORY"),
                EventName.of("REGROUPED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();
                    String group = event.getEvent().get("group").toString();
                    if (group.equals("CLOTHING")) {
                        return Mono.empty();
                    }

                    return module.markCategoryAsUnavailable(categoryId);
                }
        );
    }

    @Bean
    public EventListener onCategoryDeletedMarkCategoryAsUnavailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-deleted-mark-category-as-unavailable",
                AggregateType.of("CATEGORY"),
                EventName.of("DELETED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.markCategoryAsUnavailable(categoryId);
                }
        );
    }

}
