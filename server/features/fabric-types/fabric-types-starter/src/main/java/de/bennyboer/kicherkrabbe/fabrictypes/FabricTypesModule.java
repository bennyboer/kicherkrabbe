package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.FabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.LookupFabricType;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.fabrictypes.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class FabricTypesModule {

    private final FabricTypeService fabricTypeService;

    private final PermissionsService permissionsService;

    private final FabricTypeLookupRepo fabricTypeLookupRepo;

    private final ResourceChangesTracker changesTracker;

    public Mono<FabricTypesPage> getFabricTypes(String searchTerm, long skip, long limit, Agent agent) {
        return getAccessibleFabricTypeIds(agent)
                .collectList()
                .flatMap(fabricTypeIds -> fabricTypeLookupRepo.find(fabricTypeIds, searchTerm, skip, limit))
                .map(result -> FabricTypesPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults().stream().map(fabricType -> FabricTypeDetails.of(
                                fabricType.getId(),
                                fabricType.getVersion(),
                                fabricType.getName(),
                                fabricType.getCreatedAt()
                        )).toList()
                ));
    }

    public Flux<ResourceChange> getFabricTypeChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());

        return changesTracker.getChanges(receiverId);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createFabricType(String name, Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(fabricTypeService.create(FabricTypeName.of(name), agent))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateFabricType(String fabricTypeId, long version, String name, Agent agent) {
        var id = FabricTypeId.of(fabricTypeId);

        return assertAgentIsAllowedTo(agent, UPDATE, id)
                .then(fabricTypeService.update(id, Version.of(version), FabricTypeName.of(name), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> deleteFabricType(String fabricTypeId, long version, Agent agent) {
        var id = FabricTypeId.of(fabricTypeId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(fabricTypeService.delete(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    public Mono<Void> allowUserToCreateFabricTypes(String userId) {
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

    public Mono<Void> allowUserToManageFabricType(String fabricTypeId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(fabricTypeId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var updatePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                updatePermission,
                deletePermission
        );
    }

    public Mono<Void> removePermissionsForFabricType(String fabricTypeId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(fabricTypeId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> updateFabricTypeInLookup(String fabricTypeId) {
        return fabricTypeService.getOrThrow(FabricTypeId.of(fabricTypeId))
                .flatMap(fabricType -> fabricTypeLookupRepo.update(LookupFabricType.of(
                        fabricType.getId(),
                        fabricType.getVersion(),
                        fabricType.getName(),
                        fabricType.getCreatedAt()
                )))
                .then();
    }

    public Mono<Void> removeFabricTypeFromLookup(String fabricTypeId) {
        return fabricTypeLookupRepo.remove(FabricTypeId.of(fabricTypeId));
    }

    private Flux<FabricTypeId> getAccessibleFabricTypeIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> FabricTypeId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable FabricTypeId fabricTypeId) {
        Permission permission = toPermission(agent, action, fabricTypeId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable FabricTypeId fabricTypeId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(fabricTypeId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(fabricTypeId.getValue()))))
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
        return ResourceType.of("FABRIC_TYPE");
    }

}
