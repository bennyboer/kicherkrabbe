package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.categories.persistence.lookup.CategoryLookupRepo;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.LookupCategory;
import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.categories.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class CategoriesModule {

    private final CategoryService categoryService;

    private final PermissionsService permissionsService;

    private final CategoryLookupRepo categoryLookupRepo;

    private final ResourceChangesTracker changesTracker;

    @Transactional(propagation = MANDATORY)
    public Mono<String> createCategory(String name, CategoryGroup group, Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(categoryService.create(CategoryName.of(name), group, agent))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> renameCategory(String categoryId, long version, String name, Agent agent) {
        var id = CategoryId.of(categoryId);

        return assertAgentIsAllowedTo(agent, RENAME, id)
                .then(categoryService.rename(id, Version.of(version), CategoryName.of(name), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> regroupCategory(String categoryId, long version, CategoryGroup group, Agent agent) {
        var id = CategoryId.of(categoryId);

        return assertAgentIsAllowedTo(agent, REGROUP, id)
                .then(categoryService.regroup(id, Version.of(version), group, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> deleteCategory(String categoryId, long version, Agent agent) {
        var id = CategoryId.of(categoryId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(categoryService.delete(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    public Mono<CategoriesPage> getCategories(String searchTerm, long skip, long limit, Agent agent) {
        return getAccessibleCategoryIds(agent)
                .collectList()
                .flatMap(categoryIds -> categoryLookupRepo.find(categoryIds, searchTerm, skip, limit))
                .map(result -> CategoriesPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults().stream().map(category -> CategoryDetails.of(
                                category.getId(),
                                category.getVersion(),
                                category.getName(),
                                category.getGroup(),
                                category.getCreatedAt()
                        )).toList()
                ));
    }

    public Mono<CategoriesPage> getCategoriesByGroup(
            CategoryGroup group,
            String searchTerm,
            long skip,
            long limit,
            Agent agent
    ) {
        return getAccessibleCategoryIds(agent)
                .collectList()
                .flatMap(categoryIds -> categoryLookupRepo.findByGroup(categoryIds, group, searchTerm, skip, limit))
                .map(result -> CategoriesPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults().stream().map(category -> CategoryDetails.of(
                                category.getId(),
                                category.getVersion(),
                                category.getName(),
                                category.getGroup(),
                                category.getCreatedAt()
                        )).toList()
                ));
    }

    public Mono<CategoryDetails> getCategory(String categoryId, Agent agent) {
        return assertAgentIsAllowedTo(agent, READ, CategoryId.of(categoryId))
                .then(categoryLookupRepo.findById(CategoryId.of(categoryId)))
                .map(category -> CategoryDetails.of(
                        category.getId(),
                        category.getVersion(),
                        category.getName(),
                        category.getGroup(),
                        category.getCreatedAt()
                ));
    }

    public Flux<ResourceChange> getCategoryChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());

        return changesTracker.getChanges(receiverId);
    }

    public Mono<Void> allowUserToCreateCategories(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(getResourceType());

        return permissionsService.addPermission(createPermission);
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(holder);
    }

    public Mono<Void> removePermissionsForCategory(String categoryId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(categoryId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowUserToManageCategory(String categoryId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(categoryId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var renamePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(RENAME)
                .on(resource);
        var regroupPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(REGROUP)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                renamePermission,
                regroupPermission,
                deletePermission
        );
    }

    public Mono<Void> updateCategoryInLookup(String categoryId) {
        return categoryService.getOrThrow(CategoryId.of(categoryId))
                .flatMap(category -> categoryLookupRepo.update(LookupCategory.of(
                        category.getId(),
                        category.getVersion(),
                        category.getName(),
                        category.getGroup(),
                        category.getCreatedAt()
                )))
                .then();
    }

    public Mono<Void> removeCategoryFromLookup(String categoryId) {
        return categoryLookupRepo.remove(CategoryId.of(categoryId));
    }

    private Flux<CategoryId> getAccessibleCategoryIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> CategoryId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable CategoryId categoryId) {
        Permission permission = toPermission(agent, action, categoryId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable CategoryId categoryId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(categoryId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(categoryId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Holder toHolder(Agent agent) {
        if (agent.isSystem()) {
            return Holder.group(HolderId.system());
        } else if (agent.isAnonymous()) {
            return Holder.group(HolderId.anonymous());
        } else {
            return Holder.user(HolderId.of(agent.getId().getValue()));
        }
    }

    private ResourceType getResourceType() {
        return ResourceType.of("CATEGORY");
    }

}