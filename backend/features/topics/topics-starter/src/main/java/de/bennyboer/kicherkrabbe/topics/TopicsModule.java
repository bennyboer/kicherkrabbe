package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.changes.ReceiverId;
import de.bennyboer.kicherkrabbe.changes.ResourceChange;
import de.bennyboer.kicherkrabbe.changes.ResourceChangesTracker;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.LookupTopic;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.TopicLookupRepo;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.topics.Actions.*;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@AllArgsConstructor
public class TopicsModule {

    private final TopicService topicService;

    private final PermissionsService permissionsService;

    private final TopicLookupRepo topicLookupRepo;

    private final ResourceChangesTracker changesTracker;

    public Mono<TopicsPage> getTopics(String searchTerm, long skip, long limit, Agent agent) {
        return getAccessibleTopicIds(agent)
                .collectList()
                .flatMap(topicIds -> topicLookupRepo.find(topicIds, searchTerm, skip, limit))
                .map(result -> TopicsPage.of(
                        result.getSkip(),
                        result.getLimit(),
                        result.getTotal(),
                        result.getResults().stream().map(topic -> TopicDetails.of(
                                topic.getId(),
                                topic.getVersion(),
                                topic.getName(),
                                topic.getCreatedAt()
                        )).toList()
                ));
    }

    public Flux<ResourceChange> getTopicChanges(Agent agent) {
        var receiverId = ReceiverId.of(agent.getId().getValue());

        return changesTracker.getChanges(receiverId);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<String> createTopic(String name, Agent agent) {
        return assertAgentIsAllowedTo(agent, CREATE)
                .then(topicService.create(TopicName.of(name), agent))
                .map(result -> result.getId().getValue());
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> updateTopic(String topicId, long version, String name, Agent agent) {
        var id = TopicId.of(topicId);

        return assertAgentIsAllowedTo(agent, UPDATE, id)
                .then(topicService.update(id, Version.of(version), TopicName.of(name), agent))
                .map(Version::getValue);
    }

    @Transactional(propagation = MANDATORY)
    public Mono<Long> deleteTopic(String topicId, long version, Agent agent) {
        var id = TopicId.of(topicId);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(topicService.delete(id, Version.of(version), agent))
                .map(Version::getValue);
    }

    public Mono<Void> allowUserToCreateTopics(String userId) {
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

    public Mono<Void> allowUserToManageTopic(String topicId, String userId) {
        var holder = Holder.user(HolderId.of(userId));
        var resource = Resource.of(getResourceType(), ResourceId.of(topicId));

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

    public Mono<Void> removePermissionsForTopic(String topicId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(topicId));

        return permissionsService.removePermissionsByResource(resource);
    }

    public Mono<Void> updateTopicInLookup(String topicId) {
        return topicService.getOrThrow(TopicId.of(topicId))
                .flatMap(topic -> topicLookupRepo.update(LookupTopic.of(
                        topic.getId(),
                        topic.getVersion(),
                        topic.getName(),
                        topic.getCreatedAt()
                )))
                .then();
    }

    public Mono<Void> removeTopicFromLookup(String topicId) {
        return topicLookupRepo.remove(TopicId.of(topicId));
    }

    private Flux<TopicId> getAccessibleTopicIds(Agent agent) {
        Holder holder = toHolder(agent);
        ResourceType resourceType = getResourceType();

        return permissionsService.findPermissionsByHolderAndResourceType(holder, resourceType)
                .mapNotNull(permission -> permission.getResource()
                        .getId()
                        .map(id -> TopicId.of(id.getValue()))
                        .orElse(null));
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable TopicId topicId) {
        Permission permission = toPermission(agent, action, topicId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable TopicId topicId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(topicId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(topicId.getValue()))))
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
        return ResourceType.of("TOPIC");
    }

}
