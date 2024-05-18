package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.assets.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class AssetsModule {

    private final AssetService assetService;

    private final PermissionsService permissionsService;

    private final StorageService storageService;

    public Mono<AssetContent> getAssetContent(String assetId, Agent agent) {
        var id = AssetId.of(assetId);

        return assertAgentIsAllowedTo(agent, READ, id)
                .then(assetService.get(id)
                        .map(asset -> {
                            Flux<DataBuffer> buffers$ = storageService.load(id, asset.getLocation());

                            return AssetContent.of(asset.getContentType(), buffers$);
                        }));
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
                .then(assetService.getOrThrow(id))
                .delayUntil(asset -> assetService.delete(id, Version.of(version), agent))
                .delayUntil(asset -> storageService.remove(id, asset.getLocation()))
                .then();
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

    private Mono<Void> assertContentIsNotTooLarge(Flux<DataBuffer> content) {
        return content
                .map(DataBuffer::readableByteCount)
                .reduce(0L, Long::sum)
                .filter(size -> size <= 1024 * 1024 * 16)
                .switchIfEmpty(Mono.error(new AssetTooLargeError()))
                .then();
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

}
