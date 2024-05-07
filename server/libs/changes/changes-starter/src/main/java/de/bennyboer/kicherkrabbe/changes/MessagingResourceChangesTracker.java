package de.bennyboer.kicherkrabbe.changes;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.HandleableEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEvent;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventType;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.changes.ResourceChangeType.PERMISSIONS_ADDED;
import static de.bennyboer.kicherkrabbe.changes.ResourceChangeType.PERMISSIONS_REMOVED;

@AllArgsConstructor
public class MessagingResourceChangesTracker implements ResourceChangesTracker {

    EventListenerFactory eventListenerFactory;

    PermissionEventListenerFactory permissionEventListenerFactory;

    PermissionsService permissionsService;

    ResourceType resourceType;

    Action readEventsAction;

    @Override
    public Flux<ResourceChange> getChanges(ReceiverId receiverId) {
        Flux<ResourceChange> permissionChanges$ = getPermissionChanges(receiverId);
        Flux<ResourceChange> eventChanges$ = getEventChanges(receiverId);

        return Flux.merge(permissionChanges$, eventChanges$);
    }

    private Flux<ResourceChange> getEventChanges(ReceiverId receiverId) {
        String listenerName = "track-changes-for-receiver-%s".formatted(receiverId.getValue());

        return eventListenerFactory.createTransientEventListenerForAllEvents(
                        listenerName,
                        toAggregateType(resourceType)
                )
                .filterWhen(event -> permissionsService.hasPermission(toPermission(event, receiverId)))
                .map(this::toResourceChange);
    }

    private Flux<ResourceChange> getPermissionChanges(ReceiverId receiverId) {
        String listenerName = "track-permission-changes-for-receiver-%s".formatted(receiverId.getValue());

        return permissionEventListenerFactory.listen(toPermissionsResourceType(resourceType), listenerName)
                .map(event -> filterPermissionsForReceiver(event, receiverId))
                .filter(event -> !event.getPermissions().isEmpty())
                .map(this::toResourceChange);
    }

    private ResourceChange toResourceChange(PermissionEvent event) {
        ResourceChangeType type = event.getType() == PermissionEventType.ADDED
                ? PERMISSIONS_ADDED
                : PERMISSIONS_REMOVED;

        Set<ResourceId> affected = event.getPermissions()
                .stream()
                .flatMap(p -> p.getResource().getId().stream())
                .map(id -> ResourceId.of(id.getValue()))
                .collect(Collectors.toSet());

        return ResourceChange.of(type, affected);
    }

    private ResourceChange toResourceChange(HandleableEvent event) {
        var type = ResourceChangeType.of(event.getEventName().getValue());
        var affected = Set.of(ResourceId.of(event.getMetadata().getAggregateId().getValue()));

        return ResourceChange.of(type, affected); // TODO Allow to add payload describing the change in more detail
    }

    private PermissionEvent filterPermissionsForReceiver(PermissionEvent event, ReceiverId receiverId) {
        Holder holder = Holder.user(HolderId.of(receiverId.getValue()));

        Set<Permission> permissions = event.getPermissions();
        Set<Permission> filteredPermissions = permissions.stream()
                .filter(permission -> permission.getHolder().equals(holder))
                .collect(Collectors.toSet());

        if (event.getType() == PermissionEventType.ADDED) {
            return PermissionEvent.added(filteredPermissions);
        } else {
            return PermissionEvent.removed(filteredPermissions);
        }
    }

    private Permission toPermission(HandleableEvent event, ReceiverId receiverId) {
        var holder = Holder.user(HolderId.of(receiverId.getValue()));

        EventMetadata metadata = event.getMetadata();
        var resourceType = de.bennyboer.kicherkrabbe.permissions.ResourceType.of(metadata.getAggregateType()
                .getValue());
        var resourceId = de.bennyboer.kicherkrabbe.permissions.ResourceId.of(metadata.getAggregateId().getValue());
        var resource = Resource.of(resourceType, resourceId);

        return Permission.builder()
                .holder(holder)
                .isAllowedTo(readEventsAction)
                .on(resource);
    }

    private de.bennyboer.kicherkrabbe.permissions.ResourceType toPermissionsResourceType(ResourceType resourceType) {
        return de.bennyboer.kicherkrabbe.permissions.ResourceType.of(resourceType.getValue());
    }

    private AggregateType toAggregateType(ResourceType resourceType) {
        return AggregateType.of(resourceType.getValue());
    }

}
