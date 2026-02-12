package de.bennyboer.kicherkrabbe.categories.messaging;

import de.bennyboer.kicherkrabbe.categories.CategoriesModule;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class CategoriesMessaging {

    @Bean("categories_onUserCreatedAllowUserToCreateCategories")
    public EventListener onUserCreatedAllowUserToCreateCategories(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.user-created-allow-user-to-create-categories",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToCreateCategories(userId);
                }
        );
    }

    @Bean("categories_onUserDeletedRemoveCategoriesPermissionsForUser")
    public EventListener onUserDeletedRemoveCategoriesPermissionsForUser(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("categories_onCategoryCreatedUpdateLookup")
    public EventListener onCategoryCreatedUpdateLookup(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.category-created-update-lookup",
                AggregateType.of("CATEGORY"),
                EventName.of("CREATED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.updateCategoryInLookup(categoryId);
                }
        );
    }

    @Bean("categories_onCategoryRenamedUpdateLookup")
    public EventListener onCategoryRenamedUpdateLookup(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.category-renamed-update-lookup",
                AggregateType.of("CATEGORY"),
                EventName.of("RENAMED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.updateCategoryInLookup(categoryId);
                }
        );
    }

    @Bean("categories_onCategoryRegroupedUpdateLookup")
    public EventListener onCategoryRegroupedUpdateLookup(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.category-regrouped-update-lookup",
                AggregateType.of("CATEGORY"),
                EventName.of("REGROUPED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.updateCategoryInLookup(categoryId);
                }
        );
    }

    @Bean("categories_onCategoryDeletedRemoveCategoryFromLookup")
    public EventListener onCategoryDeletedRemoveCategoryFromLookup(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.category-deleted-remove-category-from-lookup",
                AggregateType.of("CATEGORY"),
                EventName.of("DELETED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.removeCategoryFromLookup(categoryId);
                }
        );
    }

    @Bean("categories_onCategoryCreatedAllowUserToManageCategory")
    public EventListener onCategoryCreatedAllowUserToManageCategory(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.category-created-allow-user-to-manage-category",
                AggregateType.of("CATEGORY"),
                EventName.of("CREATED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    Agent agent = event.getMetadata().getAgent();
                    if (agent.getType() == AgentType.USER) {
                        String userId = agent.getId().getValue();
                        return module.allowUserToManageCategory(categoryId, userId);
                    }

                    return Mono.empty();
                }
        );
    }

    @Bean("categories_onCategoryDeletedRemovePermissionsForCategory")
    public EventListener onCategoryDeletedRemovePermissionsForCategory(
            EventListenerFactory factory,
            CategoriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "categories.category-deleted-remove-permissions",
                AggregateType.of("CATEGORY"),
                EventName.of("DELETED"),
                (event) -> {
                    String categoryId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForCategory(categoryId);
                }
        );
    }

}
