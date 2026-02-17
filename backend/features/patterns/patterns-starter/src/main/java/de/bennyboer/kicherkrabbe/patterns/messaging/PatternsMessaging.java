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

    record CategoryEvent(String name) {
    }

    record CategoryRegroupedEvent(String name, String group) {
    }

    @Bean("patterns_onUserCreatedAllowUserToCreatePatterns")
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

    @Bean("patterns_onUserDeletedRemovePatternsPermissionsForUser")
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

    @Bean("patterns_onPatternCreatedOrUpdatedUpdateLookup")
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

    @Bean("patterns_onPatternDeletedRemoveFromLookup")
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

    @Bean("patterns_onPatternCreatedAllowUserToManagePattern")
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

    @Bean("patterns_onPatternDeletedRemovePermissionsOnPattern")
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

    @Bean("patterns_onPatternPublishedAllowAnonymousAndSystemUsersToReadPublishedPattern")
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

    @Bean("patterns_onPatternUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedPattern")
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

    @Bean("patterns_onPatternFeaturedAllowAnonymousAndSystemUsersToReadFeaturedPattern")
    public EventListener onPatternFeaturedAllowAnonymousAndSystemUsersToReadFeaturedPattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-featured-allow-anonymous-and-system-users-to-read-featured-pattern",
                AggregateType.of("PATTERN"),
                EventName.of("FEATURED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadFeaturedPattern(patternId);
                }
        );
    }

    @Bean("patterns_onPatternUnfeaturedDisallowAnonymousAndSystemUsersToReadFeaturedPattern")
    public EventListener onPatternUnfeaturedDisallowAnonymousAndSystemUsersToReadFeaturedPattern(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.pattern-unfeatured-disallow-anonymous-and-system-users-to-read-featured-pattern",
                AggregateType.of("PATTERN"),
                EventName.of("UNFEATURED"),
                (event) -> {
                    String patternId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadFeaturedPattern(patternId);
                }
        );
    }

    @Bean("patterns_onCategoryDeletedRemoveCategoryFromPatterns")
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

    @Bean("patterns_onCategoryCreatedMarkCategoryAsAvailable")
    public EventListener onCategoryCreatedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-created-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("CREATED"),
                CategoryEvent.class,
                (metadata, event) -> {
                    String categoryId = metadata.getAggregateId().getValue();

                    return module.markCategoryAsAvailable(categoryId, event.name());
                }
        );
    }

    @Bean("patterns_onCategoryRenamedMarkCategoryAsAvailable")
    public EventListener onCategoryRenamedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-renamed-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("RENAMED"),
                CategoryEvent.class,
                (metadata, event) -> {
                    String categoryId = metadata.getAggregateId().getValue();

                    return module.markCategoryAsAvailable(categoryId, event.name());
                }
        );
    }

    @Bean("patterns_onCategoryRegroupedMarkCategoryAsAvailable")
    public EventListener onCategoryRegroupedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-regrouped-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("REGROUPED"),
                CategoryRegroupedEvent.class,
                (metadata, event) -> {
                    if (!"CLOTHING".equals(event.group())) {
                        return Mono.empty();
                    }

                    String categoryId = metadata.getAggregateId().getValue();
                    return module.markCategoryAsAvailable(categoryId, event.name());
                }
        );
    }

    @Bean("patterns_onCategoryRegroupedToNonClothingCategoryMarkCategoryAsUnavailable")
    public EventListener onCategoryRegroupedToNonClothingCategoryMarkCategoryAsUnavailable(
            EventListenerFactory factory,
            PatternsModule module
    ) {
        return factory.createEventListenerForEvent(
                "patterns.category-regrouped-to-non-clothing-mark-category-as-unavailable",
                AggregateType.of("CATEGORY"),
                EventName.of("REGROUPED"),
                CategoryRegroupedEvent.class,
                (metadata, event) -> {
                    if ("CLOTHING".equals(event.group())) {
                        return Mono.empty();
                    }

                    String categoryId = metadata.getAggregateId().getValue();
                    return module.markCategoryAsUnavailable(categoryId);
                }
        );
    }

    @Bean("patterns_onCategoryDeletedMarkCategoryAsUnavailable")
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
