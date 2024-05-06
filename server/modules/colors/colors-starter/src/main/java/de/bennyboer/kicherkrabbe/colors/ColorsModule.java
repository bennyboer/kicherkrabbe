package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.colors.Actions.*;

@AllArgsConstructor
public class ColorsModule {

    private final ColorService colorService;

    private final PermissionsService permissionsService;

    private final ColorLookupRepo colorLookupRepo;

    public Flux<ColorDetails> getColors(String searchTerm, long skip, long limit, Agent agent) {
        return assertAgentIsAllowedTo(agent, READ)
                .thenMany(colorLookupRepo.find(searchTerm, skip, limit))
                .map(lookupColor -> ColorDetails.of(
                        lookupColor.getId(),
                        lookupColor.getName(),
                        lookupColor.getRed(),
                        lookupColor.getGreen(),
                        lookupColor.getBlue(),
                        lookupColor.getCreatedAt()
                ));
    }

    @Transactional
    public Mono<String> createColor(String name, int red, int green, int blue, Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(colorService.create(ColorName.of(name), red, green, blue, agent))
                .map(AggregateIdAndVersion::getId)
                .map(ColorId::getValue);
    }

    @Transactional
    public Mono<Long> updateColor(
            String colorId,
            long version,
            String name,
            int red,
            int green,
            int blue,
            Agent agent
    ) {
        var id = ColorId.of(colorId);

        return assertAgentIsAllowedTo(agent, UPDATE, id)
                .then(colorService.update(id, Version.of(version), ColorName.of(name), red, green, blue, agent))
                .map(Version::getValue);
    }

    @Transactional
    public Mono<Long> deleteColor(String colorId, long version, Agent agent) {
        var id = ColorId.of(colorId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(colorService.delete(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    public Mono<Void> allowUserToCreateAndReadColors(String userId) {
        Holder userHolder = Holder.user(HolderId.of(userId));

        var createPermission = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(CREATE)
                .onType(getResourceType());
        var readPermission = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getResourceType());

        return permissionsService.addPermissions(
                createPermission,
                readPermission
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        Holder userHolder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(userHolder);
    }

    public Mono<Void> updateColorInLookup(String colorId) {
        return colorService.get(ColorId.of(colorId))
                .flatMap(color -> colorLookupRepo.update(LookupColor.of(
                        color.getId(),
                        color.getName(),
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        color.getCreatedAt()
                )));
    }

    public Mono<Void> removeColorFromLookup(String colorId) {
        return colorLookupRepo.remove(ColorId.of(colorId));
    }

    public Mono<Void> allowCreatorToManageColor(String colorId, String userId) {
        Holder user = Holder.user(HolderId.of(userId));
        Resource resource = Resource.of(getResourceType(), ResourceId.of(colorId));

        var readAsUser = Permission.builder()
                .holder(user)
                .isAllowedTo(READ)
                .on(resource);
        var updateAsUser = Permission.builder()
                .holder(user)
                .isAllowedTo(Actions.UPDATE)
                .on(resource);
        var deleteAsUser = Permission.builder()
                .holder(user)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readAsUser,
                updateAsUser,
                deleteAsUser
        );
    }

    public Mono<Void> removePermissionsForColor(String colorId) {
        Resource resource = Resource.of(getResourceType(), ResourceId.of(colorId));

        return permissionsService.removePermissionsByResource(resource);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable ColorId colorId) {
        Permission permission = toPermission(agent, action, colorId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable ColorId colorId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(colorId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(colorId.getValue()))))
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
        return ResourceType.of("COLOR");
    }

}