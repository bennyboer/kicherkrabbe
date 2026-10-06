package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.EmbroideryCategoryRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.EmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.embroideries.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class EmbroideriesModule {

    private final EmbroideryService embroideryService;

    private final PermissionsService permissionsService;

    private final EmbroideryLookupRepo embroideryLookupRepo;

    private final ResourceChangesTracker changesTracker;

    private final EmbroideryCategoryRepo embroideryCategoryRepo;

    public Mono<EmbroideriesPage> getEmbroideries(
            String searchTerm,
            Set<String> categories,
            long skip,
            long limit,
            Agent agent
    ) {
        Set<EmbroideryCategoryId> internalCategories = toInternalCategories(categories);

        return getAccessibleEmbroideryIds(agent)
                .collectList()
                .flatMap(ids -> embroideryLookupRepo.find(ids, internalCategories, searchTerm, skip, limit))
                .map(result -> EmbroideriesPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(this::toEmbroideryDetails)
                                .toList()
                ));
    }

    public Mono<EmbroideryDetails> getEmbroidery(String embroideryId, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);

        return assertAgentIsAllowedTo(agent, READ, internalId)
                .then(embroideryLookupRepo.findById(internalId))
                .switchIfEmpty(Mono.error(new EmbroideryNotFoundError(internalId)))
                .map(this::toEmbroideryDetails);
    }

    public Mono<PublishedEmbroideriesPage> getPublishedEmbroideries(
            String searchTerm,
            Set<String> categories,
            boolean ascending,
            long skip,
            long limit,
            Agent ignoredAgent
    ) {
        Set<EmbroideryCategoryId> internalCategories = toInternalCategories(categories);

        return embroideryLookupRepo.findPublished(searchTerm, internalCategories, ascending, skip, limit)
                .map(result -> PublishedEmbroideriesPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(this::toPublishedEmbroidery)
                                .toList()
                ));
    }

    public Mono<PublishedEmbroidery> getPublishedEmbroidery(String embroideryIdOrAlias, Agent agent) {
        return embroideryLookupRepo.findPublishedByAlias(EmbroideryAlias.of(embroideryIdOrAlias))
                .switchIfEmpty(Mono.defer(() -> embroideryLookupRepo.findPublished(EmbroideryId.of(embroideryIdOrAlias))))
                .flatMap(embroidery -> assertAgentIsAllowedTo(agent, READ_PUBLISHED, embroidery.getId())
                        .thenReturn(embroidery))
                .map(this::toPublishedEmbroidery)
                .onErrorResume(MissingPermissionError.class, _ -> Mono.empty());
    }

    public Flux<PublishedEmbroidery> getFeaturedEmbroideries(Agent ignoredAgent) {
        return embroideryLookupRepo.findFeatured()
                .map(this::toPublishedEmbroidery);
    }

    public Flux<EmbroideryCategory> getAvailableCategoriesForEmbroideries(Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .thenMany(embroideryCategoryRepo.findAll());
    }

    public Flux<EmbroideryCategory> getCategoriesUsedInEmbroideries(Agent ignoredAgent) {
        return embroideryLookupRepo.findUniqueCategories()
                .collect(Collectors.toSet())
                .flatMapMany(embroideryCategoryRepo::findByIds);
    }

    public Flux<ResourceChange> getEmbroideryChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());

        return changesTracker.getChanges(receiverId);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createEmbroidery(String name, String image, Set<String> categories, Agent agent) {
        notNull(name, "Embroidery name must be given");
        check(!name.isBlank(), "Embroidery name must not be blank");
        notNull(image, "Image must be given");
        notNull(categories, "Categories must be given");

        var internalName = EmbroideryName.of(name);
        var internalImage = ImageId.of(image);
        var internalCategories = toInternalCategories(categories);
        var alias = EmbroideryAlias.fromName(internalName);

        return assertAgentIsAllowedTo(agent, CREATE)
                .then(assertCategoriesAvailable(internalCategories))
                .then(assertAliasIsNotAlreadyInUse(alias, null))
                .then(embroideryService.create(internalName, internalImage, internalCategories, agent))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> renameEmbroidery(String embroideryId, long version, String name, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);
        var internalName = EmbroideryName.of(name);
        var alias = EmbroideryAlias.fromName(internalName);

        return assertAgentIsAllowedTo(agent, RENAME, internalId)
                .then(assertAliasIsNotAlreadyInUse(alias, internalId))
                .then(embroideryService.rename(internalId, Version.of(version), internalName, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> publishEmbroidery(String embroideryId, long version, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);

        return assertAgentIsAllowedTo(agent, PUBLISH, internalId)
                .then(embroideryService.publish(internalId, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unpublishEmbroidery(String embroideryId, long version, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);

        return assertAgentIsAllowedTo(agent, UNPUBLISH, internalId)
                .then(embroideryService.unpublish(internalId, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> featureEmbroidery(String embroideryId, long version, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);

        return assertAgentIsAllowedTo(agent, FEATURE, internalId)
                .then(embroideryService.feature(internalId, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unfeatureEmbroidery(String embroideryId, long version, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);

        return assertAgentIsAllowedTo(agent, UNFEATURE, internalId)
                .then(embroideryService.unfeature(internalId, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateEmbroideryImage(String embroideryId, long version, String image, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);
        var internalImage = ImageId.of(image);

        return assertAgentIsAllowedTo(agent, UPDATE_IMAGE, internalId)
                .then(embroideryService.updateImage(internalId, Version.of(version), internalImage, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateEmbroideryCategories(
            String embroideryId,
            long version,
            Set<String> categories,
            Agent agent
    ) {
        var internalId = EmbroideryId.of(embroideryId);
        var internalCategories = toInternalCategories(categories);

        return assertAgentIsAllowedTo(agent, UPDATE_CATEGORIES, internalId)
                .then(assertCategoriesAvailable(internalCategories))
                .then(embroideryService.updateCategories(internalId, Version.of(version), internalCategories, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> deleteEmbroidery(String embroideryId, long version, Agent agent) {
        var internalId = EmbroideryId.of(embroideryId);

        return assertAgentIsAllowedTo(agent, DELETE, internalId)
                .then(embroideryService.delete(internalId, Version.of(version), agent))
                .then();
    }

    @Transactional(propagation = MANDATORY)
    public Flux<String> removeCategoryFromEmbroideries(String categoryId, Agent agent) {
        var internalCategoryId = EmbroideryCategoryId.of(categoryId);

        return embroideryLookupRepo.findByCategory(internalCategoryId)
                .delayUntil(embroidery -> embroideryService.removeCategory(
                        embroidery.getId(),
                        embroidery.getVersion(),
                        internalCategoryId,
                        agent
                ))
                .map(embroidery -> embroidery.getId().getValue());
    }

    public Mono<Void> allowUserToCreateEmbroideries(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(getResourceType());

        return permissionsService.addPermission(createPermission);
    }

    public Mono<Void> allowUserToManageEmbroidery(String embroideryId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(embroideryId));

        Set<Action> actions = Set.of(
                READ,
                READ_PUBLISHED,
                RENAME,
                PUBLISH,
                UNPUBLISH,
                FEATURE,
                UNFEATURE,
                UPDATE_IMAGE,
                UPDATE_CATEGORIES,
                DELETE
        );
        var permissions = actions.stream()
                .map(action -> Permission.builder()
                        .holder(holder)
                        .isAllowedTo(action)
                        .on(resource))
                .collect(Collectors.toSet());

        return permissionsService.addPermissions(permissions);
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(holder);
    }

    public Mono<Void> removePermissionsOnEmbroidery(String embroideryId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(embroideryId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowAnonymousAndSystemUsersToReadPublishedEmbroidery(String embroideryId) {
        return permissionsService.addPermissions(toReadPublishedPermissionsForAnonymousAndSystemUsers(embroideryId));
    }

    public Mono<Void> disallowAnonymousAndSystemUsersToReadPublishedEmbroidery(String embroideryId) {
        return permissionsService.removePermissions(toReadPublishedPermissionsForAnonymousAndSystemUsers(embroideryId));
    }

    public Mono<Void> updateEmbroideryInLookup(String embroideryId) {
        return embroideryService.getOrThrow(EmbroideryId.of(embroideryId))
                .flatMap(embroidery -> embroideryLookupRepo.update(LookupEmbroidery.of(
                        embroidery.getId(),
                        embroidery.getVersion(),
                        embroidery.isPublished(),
                        embroidery.isFeatured(),
                        embroidery.getName(),
                        EmbroideryAlias.fromName(embroidery.getName()),
                        embroidery.getImage(),
                        embroidery.getCategories(),
                        embroidery.getCreatedAt()
                )));
    }

    public Mono<Void> removeEmbroideryFromLookup(String embroideryId) {
        return embroideryLookupRepo.remove(EmbroideryId.of(embroideryId));
    }

    public Mono<Void> markCategoryAsAvailable(String id, String name) {
        var category = EmbroideryCategory.of(EmbroideryCategoryId.of(id), EmbroideryCategoryName.of(name));

        return embroideryCategoryRepo.save(category).then();
    }

    public Mono<Void> markCategoryAsUnavailable(String id) {
        return embroideryCategoryRepo.removeById(EmbroideryCategoryId.of(id));
    }

    private Permission[] toReadPublishedPermissionsForAnonymousAndSystemUsers(String embroideryId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(embroideryId));

        var anonymousPermission = Permission.builder()
                .holder(Holder.group(HolderId.anonymous()))
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var systemPermission = Permission.builder()
                .holder(Holder.group(HolderId.system()))
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);

        return new Permission[]{anonymousPermission, systemPermission};
    }

    private EmbroideryDetails toEmbroideryDetails(LookupEmbroidery embroidery) {
        return EmbroideryDetails.of(
                embroidery.getId(),
                embroidery.getVersion(),
                embroidery.isPublished(),
                embroidery.isFeatured(),
                embroidery.getName(),
                embroidery.getImage(),
                embroidery.getCategories(),
                embroidery.getCreatedAt()
        );
    }

    private PublishedEmbroidery toPublishedEmbroidery(LookupEmbroidery embroidery) {
        return PublishedEmbroidery.of(
                embroidery.getId(),
                embroidery.getName(),
                embroidery.getAlias(),
                embroidery.getImage(),
                embroidery.getCategories()
        );
    }

    private Flux<EmbroideryId> getAccessibleEmbroideryIds(Agent agent) {
        Holder holder = toHolder(agent);

        return permissionsService.findPermissionsByHolderAndResourceType(holder, getResourceType())
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> EmbroideryId.of(id.getValue()))
                        .orElse(null))
                .distinct();
    }

    private Mono<Void> assertCategoriesAvailable(Set<EmbroideryCategoryId> categories) {
        return embroideryCategoryRepo.findByIds(categories)
                .map(EmbroideryCategory::getId)
                .collect(Collectors.toSet())
                .flatMap(foundIds -> {
                    if (foundIds.equals(categories)) {
                        return Mono.empty();
                    }

                    Set<EmbroideryCategoryId> missingCategories = new HashSet<>(categories);
                    missingCategories.removeAll(foundIds);

                    return Mono.error(new CategoriesMissingError(missingCategories));
                });
    }

    private Mono<Void> assertAliasIsNotAlreadyInUse(EmbroideryAlias alias, @Nullable EmbroideryId excludeId) {
        return embroideryLookupRepo.findByAlias(alias)
                .filter(embroidery -> !embroidery.getId().equals(excludeId))
                .map(LookupEmbroidery::getId)
                .flatMap(conflictingId -> Mono.error(new AliasAlreadyInUseError(conflictingId, alias)));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable EmbroideryId embroideryId) {
        Permission permission = toPermission(agent, action, embroideryId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable EmbroideryId embroideryId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(embroideryId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(id.getValue()))))
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
        return ResourceType.of("EMBROIDERY");
    }

    private Set<EmbroideryCategoryId> toInternalCategories(Set<String> categories) {
        notNull(categories, "Categories must be given");

        return categories.stream()
                .map(EmbroideryCategoryId::of)
                .collect(Collectors.toSet());
    }

}
