package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsAvailabilityFilterDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDTO;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabric;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.fabrics.Actions.*;
import static de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDirectionDTO.ASCENDING;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class FabricsModule {

    private final FabricService fabricService;

    private final PermissionsService permissionsService;

    private final FabricLookupRepo fabricLookupRepo;

    public Mono<FabricDetails> getFabric(String fabricId, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, READ, id)
                .then(fabricService.getOrThrow(id))
                .map(fabric -> FabricDetails.of(
                        fabric.getId(),
                        fabric.getVersion(),
                        fabric.getName(),
                        fabric.getImage(),
                        fabric.getColors(),
                        fabric.getTopics(),
                        fabric.getAvailability(),
                        fabric.isPublished(),
                        fabric.getCreatedAt()
                ));
    }

    public Mono<FabricsPage> getFabrics(String searchTerm, long skip, long limit, Agent agent) {
        return getAccessibleFabricIds(agent)
                .collectList()
                .flatMap(topicIds -> fabricLookupRepo.find(topicIds, searchTerm, skip, limit))
                .map(result -> FabricsPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(fabric -> FabricDetails.of(
                                        fabric.getId(),
                                        fabric.getVersion(),
                                        fabric.getName(),
                                        fabric.getImage(),
                                        fabric.getColors(),
                                        fabric.getTopics(),
                                        fabric.getAvailability(),
                                        fabric.isPublished(),
                                        fabric.getCreatedAt()
                                )).toList()
                ));
    }

    public Mono<PublishedFabric> getPublishedFabric(String fabricId, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, READ_PUBLISHED, id)
                .then(fabricLookupRepo.findPublished(id))
                .map(fabric -> PublishedFabric.of(
                        fabric.getId(),
                        fabric.getName(),
                        fabric.getImage(),
                        fabric.getColors(),
                        fabric.getTopics(),
                        fabric.getAvailability()
                ))
                .onErrorResume(MissingPermissionError.class, e -> Mono.empty());
    }

    public Mono<PublishedFabricsPage> getPublishedFabrics(
            String searchTerm,
            Set<String> colorIds,
            Set<String> topicIds,
            FabricsAvailabilityFilterDTO availability,
            FabricsSortDTO sort,
            long skip,
            long limit,
            Agent ignoredAgent
    ) {
        Set<ColorId> colors = colorIds.stream()
                .map(ColorId::of)
                .collect(Collectors.toSet());
        Set<TopicId> topics = topicIds.stream()
                .map(TopicId::of)
                .collect(Collectors.toSet());
        boolean filterAvailability = availability.active;
        boolean inStock = availability.inStock;
        boolean sortAscending = sort.direction == ASCENDING;

        return fabricLookupRepo.findPublished(
                        searchTerm,
                        colors,
                        topics,
                        filterAvailability,
                        inStock,
                        sortAscending,
                        skip,
                        limit
                )
                .map(result -> PublishedFabricsPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults()
                                .stream()
                                .map(fabric -> PublishedFabric.of(
                                        fabric.getId(),
                                        fabric.getName(),
                                        fabric.getImage(),
                                        fabric.getColors(),
                                        fabric.getTopics(),
                                        fabric.getAvailability()
                                )).toList()
                ));
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createFabric(
            String name,
            String imageId,
            Set<String> colorIds,
            Set<String> topicIds,
            Set<FabricTypeAvailabilityDTO> availability,
            Agent agent
    ) {
        notNull(name, "Fabric name must be given");
        check(!name.isBlank(), "Fabric name must not be blank");
        notNull(imageId, "Image ID must be given");
        check(!imageId.isBlank(), "Image ID must not be blank");
        notNull(colorIds, "Color IDs must be given");
        notNull(topicIds, "Topic IDs must be given");
        notNull(availability, "Availability must be given");

        Set<ColorId> colors = colorIds.stream()
                .map(ColorId::of)
                .collect(Collectors.toSet());
        Set<TopicId> topics = topicIds.stream()
                .map(TopicId::of)
                .collect(Collectors.toSet());
        Set<FabricTypeAvailability> availabilities = availability.stream()
                .map(a -> FabricTypeAvailability.of(
                        FabricTypeId.of(a.typeId),
                        a.inStock
                ))
                .collect(Collectors.toSet());

        return assertAgentIsAllowedTo(agent, CREATE)
                .then(fabricService.create(
                        FabricName.of(name),
                        ImageId.of(imageId),
                        colors,
                        topics,
                        availabilities,
                        agent
                ))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Void> deleteFabric(String fabricId, long version, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(fabricService.delete(id, Version.of(version), agent))
                .then();
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> renameFabric(String fabricId, long version, String name, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, RENAME, id)
                .then(fabricService.rename(id, Version.of(version), FabricName.of(name), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> publishFabric(String fabricId, long version, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, PUBLISH, id)
                .then(fabricService.publish(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unpublishFabric(String fabricId, long version, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, UNPUBLISH, id)
                .then(fabricService.unpublish(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateFabricImage(String fabricId, long version, String imageId, Agent agent) {
        var id = FabricId.of(fabricId);

        return assertAgentIsAllowedTo(agent, UPDATE_IMAGE, id)
                .then(fabricService.updateImage(id, Version.of(version), ImageId.of(imageId), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateFabricColors(String fabricId, long version, Set<String> colorIds, Agent agent) {
        var id = FabricId.of(fabricId);
        var colors = colorIds.stream()
                .map(ColorId::of)
                .collect(Collectors.toSet());

        return assertAgentIsAllowedTo(agent, UPDATE_COLORS, id)
                .then(fabricService.updateColors(id, Version.of(version), colors, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateFabricTopics(String fabricId, long version, Set<String> topicIds, Agent agent) {
        var id = FabricId.of(fabricId);
        var topics = topicIds.stream()
                .map(TopicId::of)
                .collect(Collectors.toSet());

        return assertAgentIsAllowedTo(agent, UPDATE_TOPICS, id)
                .then(fabricService.updateTopics(id, Version.of(version), topics, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateFabricAvailability(
            String fabricId,
            long version,
            Set<FabricTypeAvailabilityDTO> availability,
            Agent agent
    ) {
        var id = FabricId.of(fabricId);
        var availabilities = availability.stream()
                .map(a -> FabricTypeAvailability.of(
                        FabricTypeId.of(a.typeId),
                        a.inStock
                ))
                .collect(Collectors.toSet());

        return assertAgentIsAllowedTo(agent, UPDATE_AVAILABILITY, id)
                .then(fabricService.updateAvailability(id, Version.of(version), availabilities, agent))
                .map(Version::getValue);
    }

    public Mono<Void> allowUserToCreateFabrics(String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resourceType = getResourceType();

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(resourceType);

        return permissionsService.addPermission(createPermission);
    }

    public Mono<Void> updateFabricInLookup(String fabricId) {
        return fabricService.getOrThrow(FabricId.of(fabricId))
                .map(fabric -> LookupFabric.of(
                        fabric.getId(),
                        fabric.getVersion(),
                        fabric.getName(),
                        fabric.getImage(),
                        fabric.getColors(),
                        fabric.getTopics(),
                        fabric.getAvailability(),
                        fabric.isPublished(),
                        fabric.getCreatedAt()
                ))
                .flatMap(fabricLookupRepo::update);
    }

    public Mono<Void> removeFabricFromLookup(String fabricId) {
        return fabricLookupRepo.remove(FabricId.of(fabricId));
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(holder);
    }

    public Mono<Void> removePermissionsOnFabric(String fabricId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(fabricId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowUserToManageFabric(String fabricId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(fabricId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var readPublishedPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var renamePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(RENAME)
                .on(resource);
        var publishPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(PUBLISH)
                .on(resource);
        var unpublishPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UNPUBLISH)
                .on(resource);
        var updateImagePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_IMAGE)
                .on(resource);
        var updateColorsPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_COLORS)
                .on(resource);
        var updateTopicsPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_TOPICS)
                .on(resource);
        var updateAvailabilityPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_AVAILABILITY)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                readPublishedPermission,
                renamePermission,
                publishPermission,
                unpublishPermission,
                updateImagePermission,
                updateColorsPermission,
                updateTopicsPermission,
                updateAvailabilityPermission,
                deletePermission
        );
    }

    public Mono<Void> allowAnonymousAndSystemUsersToReadPublishedFabric(String fabricId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(fabricId));
        var systemHolder = Holder.group(HolderId.system());
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var readPublishedPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var readPublishedSystemPermission = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);

        return permissionsService.addPermissions(
                readPublishedPermission,
                readPublishedSystemPermission
        );
    }

    public Mono<Void> disallowAnonymousAndSystemUsersToReadPublishedFabric(String fabricId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(fabricId));
        var systemHolder = Holder.group(HolderId.system());
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var readPublishedPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);
        var readPublishedSystemPermission = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(READ_PUBLISHED)
                .on(resource);

        return permissionsService.removePermissions(
                readPublishedPermission,
                readPublishedSystemPermission
        );
    }

    private Flux<FabricId> getAccessibleFabricIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> FabricId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable FabricId fabricId) {
        Permission permission = toPermission(agent, action, fabricId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable FabricId fabricId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(fabricId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(fabricId.getValue()))))
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
        return ResourceType.of("FABRIC");
    }

}
