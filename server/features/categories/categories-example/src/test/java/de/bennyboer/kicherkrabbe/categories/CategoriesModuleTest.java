package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.categories.persistence.lookup.CategoryLookupRepo;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.inmemory.InMemoryCategoryLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class CategoriesModuleTest {

    private final CategoriesModuleConfig config = new CategoriesModuleConfig();

    private final CategoryService categoryService = new CategoryService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final CategoryLookupRepo categoryLookupRepo = new InMemoryCategoryLookupRepo();

    private final CategoriesModule module = config.categoriesModule(
            categoryService,
            permissionsService,
            categoryLookupRepo,
            agent -> Flux.empty()
    );

    public void allowUserToCreateCategories(String userId) {
        module.allowUserToCreateCategories(userId).block();
    }

    public String createCategory(String name, CategoryGroup group, Agent agent) {
        String categoryId = module.createCategory(name, group, agent).block();

        module.updateCategoryInLookup(categoryId).block();
        if (agent.getType() == AgentType.USER) {
            module.allowUserToManageCategory(categoryId, agent.getId().getValue()).block();
        }

        return categoryId;
    }

    public long renameCategory(String categoryId, long version, String name, Agent agent) {
        var updatedVersion = module.renameCategory(categoryId, version, name, agent).block();

        module.updateCategoryInLookup(categoryId).block();

        return updatedVersion;
    }

    public long regroupCategory(String categoryId, long version, CategoryGroup group, Agent agent) {
        var updatedVersion = module.regroupCategory(categoryId, version, group, agent).block();

        module.updateCategoryInLookup(categoryId).block();

        return updatedVersion;
    }

    public long deleteCategory(String categoryId, long version, Agent agent) {
        var updatedVersion = module.deleteCategory(categoryId, version, agent).block();

        module.removeCategoryFromLookup(categoryId).block();
        module.removePermissionsForCategory(categoryId).block();

        return updatedVersion;
    }

    public List<CategoryDetails> getCategories(Agent agent) {
        return getCategories("", 0, Integer.MAX_VALUE, agent);
    }

    public List<CategoryDetails> getCategories(String searchTerm, long skip, long limit, Agent agent) {
        return module.getCategories(searchTerm, skip, limit, agent).block().getResults();
    }

    public List<CategoryDetails> getCategoriesByGroup(CategoryGroup group, Agent agent) {
        return getCategoriesByGroup(group, "", 0, Integer.MAX_VALUE, agent);
    }

    public List<CategoryDetails> getCategoriesByGroup(
            CategoryGroup group,
            String searchTerm,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getCategoriesByGroup(group, searchTerm, skip, limit, agent).block().getResults();
    }

    public CategoryDetails getCategory(String categoryId, Agent agent) {
        return module.getCategory(categoryId, agent).block();
    }

}
