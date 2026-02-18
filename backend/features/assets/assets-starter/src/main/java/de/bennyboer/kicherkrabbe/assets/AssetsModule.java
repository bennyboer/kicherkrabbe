package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.image.ImageProcessor;
import de.bennyboer.kicherkrabbe.assets.image.ImageVariantService;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetsSortDirection;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetsSortProperty;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.LookupAsset;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceRepo;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import de.bennyboer.kicherkrabbe.commons.Preconditions;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.assets.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class AssetsModule {

    private final AssetService assetService;

    private final PermissionsService permissionsService;

    private final StorageService storageService;

    private final ImageVariantService imageVariantService;

    private final AssetReferenceRepo assetReferenceRepo;

    private final AssetLookupRepo assetLookupRepo;

    private final AssetsModuleOptions options;

    public Mono<AssetsPage> getAssets(
            String searchTerm,
            Set<String> contentTypes,
            String sortProperty,
            String sortDirection,
            long skip,
            long limit,
            Agent agent
    ) {
        Set<ContentType> contentTypeSet = contentTypes.stream()
                .map(ContentType::of)
                .collect(Collectors.toSet());

        AssetsSortProperty sort = parseSort(sortProperty);
        AssetsSortDirection direction = parseDirection(sortDirection);

        return getAccessibleAssetIds(agent)
                .collectList()
                .flatMap(accessibleIds -> {
                    if (accessibleIds.isEmpty()) {
                        return Mono.just(AssetsPage.of(skip, limit, 0, List.of()));
                    }

                    Mono<Set<AssetId>> filteredIds$;
                    if (searchTerm != null && !searchTerm.isBlank()) {
                        filteredIds$ = assetReferenceRepo.findAssetIdsByResourceNameContaining(searchTerm)
                                .collectList()
                                .map(searchIds -> {
                                    Set<AssetId> accessibleSet = new HashSet<>(accessibleIds);
                                    return searchIds.stream()
                                            .filter(accessibleSet::contains)
                                            .collect(Collectors.toSet());
                                });
                    } else {
                        filteredIds$ = Mono.just(new HashSet<>(accessibleIds));
                    }

                    return filteredIds$.flatMap(ids -> {
                        if (ids.isEmpty()) {
                            return Mono.just(AssetsPage.of(skip, limit, 0, List.of()));
                        }

                        return assetLookupRepo.find(ids, contentTypeSet, sort, direction, skip, limit)
                                .flatMap(page -> {
                                    List<AssetId> pageIds = page.getResults().stream()
                                            .map(LookupAsset::getId)
                                            .toList();

                                    if (pageIds.isEmpty()) {
                                        return Mono.just(AssetsPage.of(
                                                page.getSkip(),
                                                page.getLimit(),
                                                page.getTotal(),
                                                List.of()
                                        ));
                                    }

                                    return assetReferenceRepo.findByAssetIds(pageIds)
                                            .collectList()
                                            .map(refs -> {
                                                Map<AssetId, List<AssetReference>> refsByAsset = refs.stream()
                                                        .collect(Collectors.groupingBy(AssetReference::getAssetId));

                                                List<AssetDetails> details = page.getResults().stream()
                                                        .map(asset -> AssetDetails.of(
                                                                asset.getId(),
                                                                asset.getVersion(),
                                                                asset.getContentType(),
                                                                asset.getFileSize(),
                                                                asset.getCreatedAt(),
                                                                refsByAsset.getOrDefault(asset.getId(), List.of())
                                                        ))
                                                        .toList();

                                                return AssetsPage.of(
                                                        page.getSkip(),
                                                        page.getLimit(),
                                                        page.getTotal(),
                                                        details
                                                );
                                            });
                                });
                    });
                });
    }

    public Mono<List<String>> getContentTypes(Agent agent) {
        return getAccessibleAssetIds(agent)
                .collectList()
                .flatMap(ids -> assetLookupRepo.findUniqueContentTypes(ids)
                        .map(ContentType::getValue)
                        .collectList());
    }

    public Mono<AssetContent> getAssetContent(String assetId, Agent agent) {
        return getAssetContent(assetId, null, agent);
    }

    public Mono<AssetContent> getAssetContent(String assetId, @Nullable Integer width, Agent agent) {
        var id = AssetId.of(assetId);

        return assertAgentIsAllowedTo(agent, READ, id)
                .then(assetService.get(id))
                .flatMap(asset -> {
                    if (width == null || !ImageProcessor.isImageContentType(asset.getContentType().getValue())) {
                        Flux<DataBuffer> buffers$ = storageService.load(id, asset.getLocation());
                        return Mono.just(AssetContent.of(asset.getContentType(), buffers$));
                    }

                    return imageVariantService.getOrGenerateBestMatchingVariant(
                            id,
                            asset.getLocation(),
                            asset.getContentType(),
                            width
                    );
                });
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> uploadAsset(String contentType, Flux<DataBuffer> content, Agent agent) {
        var id = AssetId.create();
        var location = Location.file(FileName.of(id.getValue()));

        return assertAgentIsAllowedTo(agent, CREATE)
                .then(assertContentIsNotTooLarge(content))
                .then(assetService.create(id, ContentType.of(contentType), location, agent))
                .delayUntil(version -> storageService.store(id, location, content))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> deleteAsset(String assetId, long version, Agent agent) {
        var id = AssetId.of(assetId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(assertAssetIsNotReferenced(id))
                .then(assetService.getOrThrow(id))
                .delayUntil(ignored -> assetService.delete(id, Version.of(version), agent))
                .delayUntil(asset -> {
                    if (ImageProcessor.isImageContentType(asset.getContentType().getValue())) {
                        return imageVariantService.removeVariants(id, asset.getLocation())
                                .then(storageService.remove(id, asset.getLocation()));
                    }
                    return storageService.remove(id, asset.getLocation());
                })
                .then();
    }

    public Mono<Void> updateAssetInLookup(String assetId) {
        var id = AssetId.of(assetId);

        return assetService.getOrThrow(id)
                .flatMap(asset -> storageService.getSize(id, asset.getLocation())
                        .map(fileSize -> LookupAsset.of(
                                id,
                                asset.getVersion(),
                                asset.getContentType(),
                                fileSize,
                                asset.getCreatedAt()
                        )))
                .flatMap(assetLookupRepo::update);
    }

    public Mono<Void> removeAssetFromLookup(String assetId) {
        return assetLookupRepo.remove(AssetId.of(assetId));
    }

    public Mono<Void> allowUserToCreateAssets(String userId) {
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

    public Mono<Void> removePermissionsOnAsset(String assetId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(assetId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowAnonymousUsersToReadAsset(String assetId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(assetId));
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var readPublishedPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(READ)
                .on(resource);

        return permissionsService.addPermission(readPublishedPermission);
    }

    public Mono<Void> disallowAnonymousUsersToReadAsset(String assetId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(assetId));
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var readPublishedPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(READ)
                .on(resource);

        return permissionsService.removePermission(readPublishedPermission);
    }

    public Mono<Void> allowUserToManageAsset(String assetId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(assetId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                deletePermission
        );
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> updateAssetReferences(
            AssetReferenceResourceType resourceType,
            AssetResourceId resourceId,
            Set<AssetId> assetIds
    ) {
        return updateAssetReferences(resourceType, resourceId, assetIds, null);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> updateAssetReferences(
            AssetReferenceResourceType resourceType,
            AssetResourceId resourceId,
            Set<AssetId> assetIds,
            @Nullable String resourceName
    ) {
        Mono<String> resolvedName$;
        if (resourceName != null) {
            resolvedName$ = Mono.just(resourceName);
        } else {
            resolvedName$ = assetReferenceRepo.findByResource(resourceType, resourceId)
                    .next()
                    .map(AssetReference::getResourceName)
                    .defaultIfEmpty("");
        }

        return resolvedName$.flatMap(name ->
                assetReferenceRepo.removeByResource(resourceType, resourceId)
                        .thenMany(Flux.fromIterable(assetIds)
                                .flatMap(assetId -> assetReferenceRepo.upsert(
                                        AssetReference.of(assetId, resourceType, resourceId, name)
                                )))
                        .then()
        );
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> updateResourceNameInReferences(
            AssetReferenceResourceType resourceType,
            AssetResourceId resourceId,
            String resourceName
    ) {
        Preconditions.notNull(resourceType, "Resource type must be given");
        Preconditions.notNull(resourceId, "Resource ID must be given");
        Preconditions.notNull(resourceName, "Resource name must be given");

        return assetReferenceRepo.updateResourceName(resourceType, resourceId, resourceName);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> removeAssetReferencesByResource(
            AssetReferenceResourceType resourceType,
            AssetResourceId resourceId
    ) {
        return assetReferenceRepo.removeByResource(resourceType, resourceId);
    }

    public Flux<AssetReference> findAssetReferences(AssetId assetId) {
        return assetReferenceRepo.findByAssetId(assetId);
    }

    public Mono<StorageInfo> getStorageInfo(Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(storageService.getTotalStorageSize())
                .map(usedBytes -> StorageInfo.of(usedBytes, options.getStorageLimitBytes()));
    }

    private Flux<AssetId> getAccessibleAssetIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> AssetId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertContentIsNotTooLarge(Flux<DataBuffer> content) {
        return content
                .map(DataBuffer::readableByteCount)
                .reduce(0L, Long::sum)
                .filter(size -> size <= 1024 * 1024 * 50)
                .switchIfEmpty(Mono.error(new AssetTooLargeError()))
                .flatMap(contentSize -> {
                    if (options.getStorageLimitBytes() <= 0) {
                        return Mono.empty();
                    }
                    return storageService.getTotalStorageSize()
                            .flatMap(currentUsage -> {
                                if (currentUsage + contentSize > options.getStorageLimitBytes()) {
                                    return Mono.error(new StorageLimitExceededError());
                                }
                                return Mono.empty();
                            });
                });
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable AssetId assetId) {
        Permission permission = toPermission(agent, action, assetId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable AssetId assetId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(assetId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(assetId.getValue()))))
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
        return ResourceType.of("ASSET");
    }

    private AssetsSortProperty parseSort(String sortProperty) {
        if (sortProperty == null || sortProperty.isBlank()) {
            return AssetsSortProperty.CREATED_AT;
        }
        return switch (sortProperty.toUpperCase()) {
            case "FILE_SIZE" -> AssetsSortProperty.FILE_SIZE;
            default -> AssetsSortProperty.CREATED_AT;
        };
    }

    private Mono<Void> assertAssetIsNotReferenced(AssetId assetId) {
        return assetReferenceRepo.findByAssetId(assetId)
                .hasElements()
                .flatMap(hasReferences -> hasReferences
                        ? Mono.error(new AssetStillReferencedError())
                        : Mono.empty());
    }

    private AssetsSortDirection parseDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return AssetsSortDirection.DESCENDING;
        }
        return switch (sortDirection.toUpperCase()) {
            case "ASCENDING" -> AssetsSortDirection.ASCENDING;
            default -> AssetsSortDirection.DESCENDING;
        };
    }

}
