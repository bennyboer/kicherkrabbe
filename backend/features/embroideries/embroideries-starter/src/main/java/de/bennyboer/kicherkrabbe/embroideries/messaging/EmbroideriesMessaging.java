package de.bennyboer.kicherkrabbe.embroideries.messaging;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideriesModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class EmbroideriesMessaging {

    private static final String EMBROIDERY_CATEGORY_GROUP = "EMBROIDERY";

    record CategoryRenamedEvent(String name) {
    }

    record CategoryCreatedEvent(String name, String group) {
    }

    record CategoryRegroupedEvent(String name, String group) {
    }

    @Bean("embroideries_onUserCreatedAllowUserToCreateEmbroideries")
    public EventListener onUserCreatedAllowUserToCreateEmbroideries(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.user-created-allow-user-to-create-embroideries",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateEmbroideries(userId);
                }
        );
    }

    @Bean("embroideries_onUserDeletedRemoveEmbroideriesPermissionsForUser")
    public EventListener onUserDeletedRemoveEmbroideriesPermissionsForUser(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("embroideries_onEmbroideryCreatedOrUpdatedUpdateLookup")
    public EventListener onEmbroideryCreatedOrUpdatedUpdateLookup(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForAllEvents(
                "embroideries.embroidery-created-or-updated-update-lookup",
                AggregateType.of("EMBROIDERY"),
                (event) -> {
                    boolean isDeleted = event.getEventName().equals(EventName.of("DELETED"));
                    if (isDeleted) {
                        return Mono.empty();
                    }

                    String embroideryId = event.getMetadata().getAggregateId().getValue();
                    return module.updateEmbroideryInLookup(embroideryId);
                }
        );
    }

    @Bean("embroideries_onEmbroideryDeletedRemoveFromLookup")
    public EventListener onEmbroideryDeletedRemoveFromLookup(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.embroidery-deleted-remove-from-lookup",
                AggregateType.of("EMBROIDERY"),
                EventName.of("DELETED"),
                (event) -> {
                    String embroideryId = event.getMetadata().getAggregateId().getValue();

                    return module.removeEmbroideryFromLookup(embroideryId);
                }
        );
    }

    @Bean("embroideries_onEmbroideryCreatedAllowUserToManageEmbroidery")
    public EventListener onEmbroideryCreatedAllowUserToManageEmbroidery(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.embroidery-created-allow-user-to-manage-embroidery",
                AggregateType.of("EMBROIDERY"),
                EventName.of("CREATED"),
                (event) -> {
                    String embroideryId = event.getMetadata().getAggregateId().getValue();
                    String userId = event.getMetadata().getAgent().getId().getValue();

                    return module.allowUserToManageEmbroidery(embroideryId, userId);
                }
        );
    }

    @Bean("embroideries_onEmbroideryDeletedRemovePermissionsOnEmbroidery")
    public EventListener onEmbroideryDeletedRemovePermissionsOnEmbroidery(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.embroidery-deleted-remove-permissions",
                AggregateType.of("EMBROIDERY"),
                EventName.of("DELETED"),
                (event) -> {
                    String embroideryId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsOnEmbroidery(embroideryId);
                }
        );
    }

    @Bean("embroideries_onEmbroideryPublishedAllowAnonymousAndSystemUsersToReadPublishedEmbroidery")
    public EventListener onEmbroideryPublishedAllowAnonymousAndSystemUsersToReadPublishedEmbroidery(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.embroidery-published-allow-anonymous-and-system-users-to-read-published-embroidery",
                AggregateType.of("EMBROIDERY"),
                EventName.of("PUBLISHED"),
                (event) -> {
                    String embroideryId = event.getMetadata().getAggregateId().getValue();

                    return module.allowAnonymousAndSystemUsersToReadPublishedEmbroidery(embroideryId);
                }
        );
    }

    @Bean("embroideries_onEmbroideryUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedEmbroidery")
    public EventListener onEmbroideryUnpublishedDisallowAnonymousAndSystemUsersToReadPublishedEmbroidery(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.embroidery-unpublished-disallow-anonymous-and-system-users-to-read-published-embroidery",
                AggregateType.of("EMBROIDERY"),
                EventName.of("UNPUBLISHED"),
                (event) -> {
                    String embroideryId = event.getMetadata().getAggregateId().getValue();

                    return module.disallowAnonymousAndSystemUsersToReadPublishedEmbroidery(embroideryId);
                }
        );
    }

    @Bean("embroideries_onCategoryDeletedRemoveCategoryFromEmbroideries")
    public EventListener onCategoryDeletedRemoveCategoryFromEmbroideries(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.category-deleted-remove-category-from-embroideries",
                AggregateType.of("CATEGORY"),
                EventName.of("DELETED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.removeCategoryFromEmbroideries(categoryId, event.getMetadata().getAgent()).then();
                }
        );
    }

    @Bean("embroideries_onCategoryCreatedMarkCategoryAsAvailable")
    public EventListener onCategoryCreatedMarkCategoryAsAvailable(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.category-created-mark-category-as-available",
                AggregateType.of("CATEGORY"),
                EventName.of("CREATED"),
                CategoryCreatedEvent.class,
                (metadata, event) -> {
                    if (!EMBROIDERY_CATEGORY_GROUP.equals(event.group())) {
                        return Mono.empty();
                    }

                    String categoryId = metadata.getAggregateId().getValue();
                    return module.markCategoryAsAvailable(categoryId, event.name());
                }
        );
    }

    @Bean("embroideries_onCategoryRenamedRenameCategoryIfAvailable")
    public EventListener onCategoryRenamedRenameCategoryIfAvailable(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.category-renamed-rename-category-if-available",
                AggregateType.of("CATEGORY"),
                EventName.of("RENAMED"),
                CategoryRenamedEvent.class,
                (metadata, event) -> {
                    String categoryId = metadata.getAggregateId().getValue();

                    return module.renameCategoryIfAvailable(categoryId, event.name());
                }
        );
    }

    @Bean("embroideries_onCategoryRegroupedUpdateCategoryAvailability")
    public EventListener onCategoryRegroupedUpdateCategoryAvailability(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.category-regrouped-update-category-availability",
                AggregateType.of("CATEGORY"),
                EventName.of("REGROUPED"),
                CategoryRegroupedEvent.class,
                (metadata, event) -> {
                    String categoryId = metadata.getAggregateId().getValue();

                    if (EMBROIDERY_CATEGORY_GROUP.equals(event.group())) {
                        return module.markCategoryAsAvailable(categoryId, event.name());
                    }

                    return module.markCategoryAsUnavailable(categoryId);
                }
        );
    }

    @Bean("embroideries_onCategoryDeletedMarkCategoryAsUnavailable")
    public EventListener onCategoryDeletedMarkCategoryAsUnavailable(
            EventListenerFactory factory,
            EmbroideriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "embroideries.category-deleted-mark-category-as-unavailable",
                AggregateType.of("CATEGORY"),
                EventName.of("DELETED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.markCategoryAsUnavailable(categoryId);
                }
        );
    }

}
