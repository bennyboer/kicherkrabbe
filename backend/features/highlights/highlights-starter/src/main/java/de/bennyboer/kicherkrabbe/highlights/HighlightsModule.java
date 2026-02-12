package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.highlights.api.LinkDTO;
import de.bennyboer.kicherkrabbe.highlights.api.requests.RemoveLinkFromLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.api.requests.UpdateLinkInLookupRequest;
import de.bennyboer.kicherkrabbe.highlights.api.responses.QueryLinksResponse;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.HighlightLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.LookupHighlight;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.LookupLink;
import de.bennyboer.kicherkrabbe.highlights.transformer.LinkTransformer;
import de.bennyboer.kicherkrabbe.highlights.transformer.LinkTypeTransformer;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.highlights.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

public class HighlightsModule {

    private final HighlightService highlightService;

    private final PermissionsService permissionsService;

    private final HighlightLookupRepo highlightLookupRepo;

    private final LinkLookupRepo linkLookupRepo;

    private final ResourceChangesTracker changesTracker;

    private final ReactiveTransactionManager transactionManager;

    private boolean isInitialized = false;

    public HighlightsModule(
            HighlightService highlightService,
            PermissionsService permissionsService,
            HighlightLookupRepo highlightLookupRepo,
            LinkLookupRepo linkLookupRepo,
            ResourceChangesTracker changesTracker,
            ReactiveTransactionManager transactionManager
    ) {
        this.highlightService = highlightService;
        this.permissionsService = permissionsService;
        this.highlightLookupRepo = highlightLookupRepo;
        this.linkLookupRepo = linkLookupRepo;
        this.changesTracker = changesTracker;
        this.transactionManager = transactionManager;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent ignoredEvent) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        initialize()
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        return allowSystemUserToUpdateAndDeleteLinks();
    }

    public Flux<HighlightDetails> getPublishedHighlights() {
        return highlightLookupRepo.findPublished()
                .map(this::toHighlightDetails);
    }

    public Mono<HighlightsPage> getHighlights(long skip, long limit, Agent agent) {
        return getAccessibleHighlightIds(agent)
                .collectList()
                .flatMap(highlightIds -> highlightLookupRepo.findAll(highlightIds, skip, limit))
                .map(result -> HighlightsPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults().stream().map(this::toHighlightDetails).toList()
                ));
    }

    public Mono<HighlightDetails> getHighlight(String highlightId, Agent agent) {
        return assertAgentIsAllowedTo(agent, READ, HighlightId.of(highlightId))
                .then(highlightLookupRepo.findById(HighlightId.of(highlightId)))
                .map(this::toHighlightDetails);
    }

    public Flux<ResourceChange> getHighlightChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());

        return changesTracker.getChanges(receiverId);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createHighlight(String imageId, long sortOrder, Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(highlightService.create(ImageId.of(imageId), sortOrder, agent))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateImage(String highlightId, long version, String imageId, Agent agent) {
        var id = HighlightId.of(highlightId);

        return assertAgentIsAllowedTo(agent, UPDATE_IMAGE, id)
                .then(highlightService.updateImage(id, Version.of(version), ImageId.of(imageId), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> addLink(String highlightId, long version, LinkType linkType, String linkId, String linkName, Agent agent) {
        var id = HighlightId.of(highlightId);
        var link = Link.of(linkType, LinkId.of(linkId), LinkName.of(linkName));

        return assertAgentIsAllowedTo(agent, ADD_LINK, id)
                .then(highlightService.addLink(id, Version.of(version), link, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> removeLink(String highlightId, long version, LinkType linkType, String linkId, Agent agent) {
        var id = HighlightId.of(highlightId);

        return assertAgentIsAllowedTo(agent, REMOVE_LINK, id)
                .then(highlightService.removeLink(id, Version.of(version), linkType, LinkId.of(linkId), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> publishHighlight(String highlightId, long version, Agent agent) {
        var id = HighlightId.of(highlightId);

        return assertAgentIsAllowedTo(agent, PUBLISH, id)
                .then(highlightService.publish(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> unpublishHighlight(String highlightId, long version, Agent agent) {
        var id = HighlightId.of(highlightId);

        return assertAgentIsAllowedTo(agent, UNPUBLISH, id)
                .then(highlightService.unpublish(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateSortOrder(String highlightId, long version, long sortOrder, Agent agent) {
        var id = HighlightId.of(highlightId);

        return assertAgentIsAllowedTo(agent, UPDATE_SORT_ORDER, id)
                .then(highlightService.updateSortOrder(id, Version.of(version), sortOrder, agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> deleteHighlight(String highlightId, long version, Agent agent) {
        var id = HighlightId.of(highlightId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(highlightService.delete(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    public Mono<QueryLinksResponse> getLinks(String searchTerm, long skip, long limit, Agent agent) {
        return assertAgentIsAllowedToOnLinks(agent, READ)
                .then(linkLookupRepo.find(searchTerm, skip, limit))
                .map(page -> {
                    var result = new QueryLinksResponse();
                    result.total = page.getTotal();
                    result.links = page.getLinks().stream()
                            .map(LinkTransformer::toApi)
                            .toList();
                    return result;
                });
    }

    @Transactional(propagation = MANDATORY)
    public Flux<String> updateLinkInLookup(UpdateLinkInLookupRequest req, Agent agent) {
        Link link = toLinkFromDTO(req.link);

        return assertAgentIsAllowedToOnLinks(agent, UPDATE)
                .then(linkLookupRepo.update(LookupLink.of(
                        toLookupLinkId(link.getType(), link.getId()),
                        Version.of(req.version),
                        link.getType(),
                        link.getId(),
                        link.getName()
                )))
                .thenMany(updateLinkInHighlights(link.getType(), link.getId(), agent))
                .map(HighlightId::getValue);
    }

    private Flux<HighlightId> updateLinkInHighlights(LinkType type, LinkId linkId, Agent agent) {
        return assertAgentIsAllowedTo(agent, UPDATE_LINKS)
                .thenMany(linkLookupRepo.findOne(type, linkId)
                        .flatMapMany(link -> highlightLookupRepo.findByLink(type, linkId)
                                .concatMap(highlight -> highlightService.updateLink(
                                        highlight.getId(),
                                        highlight.getVersion(),
                                        link,
                                        agent
                                ).map(updatedVersion -> highlight.getId()))));
    }

    public Flux<String> removeLinkFromLookup(RemoveLinkFromLookupRequest req, Agent agent) {
        LinkType linkType = LinkTypeTransformer.toDomain(req.linkType);
        LinkId linkId = LinkId.of(req.linkId);

        return assertAgentIsAllowedToOnLinks(agent, DELETE)
                .then(linkLookupRepo.remove(linkType, linkId))
                .thenMany(removeLinkFromHighlights(linkType, linkId, agent))
                .map(HighlightId::getValue);
    }

    private Flux<HighlightId> removeLinkFromHighlights(LinkType type, LinkId linkId, Agent agent) {
        return highlightLookupRepo.findByLink(type, linkId)
                .concatMap(highlight -> highlightService.removeLink(
                        highlight.getId(),
                        highlight.getVersion(),
                        type,
                        linkId,
                        agent
                ).map(updatedVersion -> highlight.getId()));
    }

    public Mono<Void> allowUserToCreateHighlightsAndReadLinks(String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var highlightResourceType = getHighlightResourceType();
        var linkResourceType = getLinkResourceType();

        var createPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(CREATE)
                .onType(highlightResourceType);
        var readLinksPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .onType(linkResourceType);

        return permissionsService.addPermissions(
                createPermission,
                readLinksPermission
        );
    }

    @Deprecated
    public Mono<Void> allowUserToCreateHighlights(String userId) {
        return allowUserToCreateHighlightsAndReadLinks(userId);
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var holder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(holder);
    }

    public Mono<Void> removePermissionsForHighlight(String highlightId) {
        var resource = Resource.of(getHighlightResourceType(), ResourceId.of(highlightId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> allowUserToManageHighlight(String highlightId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getHighlightResourceType(), ResourceId.of(highlightId));

        var readPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var updateImagePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_IMAGE)
                .on(resource);
        var addLinkPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(ADD_LINK)
                .on(resource);
        var removeLinkPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(REMOVE_LINK)
                .on(resource);
        var publishPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(PUBLISH)
                .on(resource);
        var unpublishPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UNPUBLISH)
                .on(resource);
        var updateSortOrderPermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(UPDATE_SORT_ORDER)
                .on(resource);
        var deletePermission = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(
                readPermission,
                updateImagePermission,
                addLinkPermission,
                removeLinkPermission,
                publishPermission,
                unpublishPermission,
                updateSortOrderPermission,
                deletePermission
        );
    }

    public Mono<Void> updateHighlightInLookup(String highlightId) {
        return highlightService.getOrThrow(HighlightId.of(highlightId))
                .flatMap(highlight -> highlightLookupRepo.update(LookupHighlight.of(
                        highlight.getId(),
                        highlight.getVersion(),
                        highlight.getImageId(),
                        highlight.getLinks(),
                        highlight.isPublished(),
                        highlight.getSortOrder(),
                        highlight.getCreatedAt()
                )))
                .then();
    }

    public Mono<Void> removeHighlightFromLookup(String highlightId) {
        return highlightLookupRepo.remove(HighlightId.of(highlightId));
    }

    private HighlightDetails toHighlightDetails(LookupHighlight highlight) {
        return HighlightDetails.of(
                highlight.getId(),
                highlight.getVersion(),
                highlight.getImageId(),
                highlight.getLinks(),
                highlight.isPublished(),
                highlight.getSortOrder(),
                highlight.getCreatedAt()
        );
    }

    private Flux<HighlightId> getAccessibleHighlightIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getHighlightResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> HighlightId.of(id.getValue()))
                        .orElse(null));
    }

    public Mono<Void> allowSystemUserToUpdateAndDeleteLinks() {
        var systemHolder = Holder.group(HolderId.system());
        var linkResourceType = getLinkResourceType();
        var highlightResourceType = getHighlightResourceType();

        var updateLinks = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(UPDATE)
                .onType(linkResourceType);
        var deleteLinks = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(DELETE)
                .onType(linkResourceType);
        var updateHighlightLinks = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(UPDATE_LINKS)
                .onType(highlightResourceType);

        return permissionsService.addPermissions(
                updateLinks,
                deleteLinks,
                updateHighlightLinks
        );
    }

    private Link toLinkFromDTO(LinkDTO dto) {
        return Link.of(
                LinkTypeTransformer.toDomain(dto.type),
                LinkId.of(dto.id),
                LinkName.of(dto.name)
        );
    }

    private String toLookupLinkId(LinkType type, LinkId linkId) {
        return type.name() + "_" + linkId.getValue();
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable HighlightId highlightId) {
        Permission permission = toHighlightPermission(agent, action, highlightId);
        return permissionsService.assertHasPermission(permission);
    }

    private Mono<Void> assertAgentIsAllowedToOnLinks(Agent agent, Action action) {
        Permission permission = toLinkPermission(agent, action);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toHighlightPermission(Agent agent, Action action, @Nullable HighlightId highlightId) {
        Holder holder = toHolder(agent);
        var resourceType = getHighlightResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(highlightId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(highlightId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Permission toLinkPermission(Agent agent, Action action) {
        Holder holder = toHolder(agent);
        var resourceType = getLinkResourceType();

        return Permission.builder()
                .holder(holder)
                .isAllowedTo(action)
                .onType(resourceType);
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

    private ResourceType getHighlightResourceType() {
        return ResourceType.of("HIGHLIGHT");
    }

    private ResourceType getLinkResourceType() {
        return ResourceType.of("HIGHLIGHT_LINK");
    }

}
