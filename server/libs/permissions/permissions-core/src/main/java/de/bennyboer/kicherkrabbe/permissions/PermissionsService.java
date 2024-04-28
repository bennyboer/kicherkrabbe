package de.bennyboer.kicherkrabbe.permissions;

import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.cache.CacheablePermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.permissions.events.PermissionEvent.added;
import static de.bennyboer.kicherkrabbe.permissions.events.PermissionEvent.removed;

public class PermissionsService {

    private final PermissionsRepo permissionsRepo;

    private final PermissionsEventPublisher eventPublisher;

    public PermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        this.permissionsRepo = new CacheablePermissionsRepo(
                permissionsRepo,
                CacheablePermissionsRepo.Config.builder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build()
        );
        this.eventPublisher = eventPublisher;
    }

    public Mono<Void> addPermission(Permission permission) {
        return permissionsRepo.insert(permission)
                .flatMap(addedPermission -> eventPublisher.publish(added(addedPermission)));
    }

    public Mono<Void> addPermissions(Set<Permission> permissions) {
        return permissionsRepo.insertAll(permissions)
                .collect(Collectors.toSet())
                .flatMap(addedPermissions -> eventPublisher.publish(added(addedPermissions)));
    }

    public Mono<Boolean> hasPermission(Permission permission) {
        return permissionsRepo.hasPermission(permission);
    }

    public Flux<Permission> findPermissionsByHolder(Holder holder) {
        return permissionsRepo.findPermissionsByHolder(holder);
    }

    public Flux<Permission> findPermissionsByHolderAndResourceType(Holder holder, ResourceType resourceType) {
        return permissionsRepo.findPermissionsByHolderAndResourceType(holder, resourceType);
    }

    public Flux<Permission> findPermissionsByHolderAndResource(Holder holder, Resource resource) {
        return permissionsRepo.findPermissionsByHolderAndResource(holder, resource);
    }

    public Flux<Permission> findPermissionsByHolderAndResourceTypeAndAction(
            Holder holder,
            ResourceType resourceType,
            Action action
    ) {
        return permissionsRepo.findPermissionsByHolderAndResourceTypeAndAction(holder, resourceType, action);
    }

    public Mono<Void> removePermission(Permission permission) {
        return permissionsRepo.removeByPermission(permission)
                .flatMap(removedPermission -> eventPublisher.publish(removed(removedPermission)));
    }

    public Mono<Void> removePermissionsByHolder(Holder holder) {
        return permissionsRepo.removeByHolder(holder)
                .collect(Collectors.toSet())
                .flatMap(permissions -> eventPublisher.publish(removed(permissions)));
    }

    public Mono<Void> removePermissionsByResource(Resource resource) {
        return permissionsRepo.removeByResource(resource)
                .collect(Collectors.toSet())
                .flatMap(permissions -> eventPublisher.publish(removed(permissions)));
    }

    public Mono<Void> removePermissionsByHolderAndResource(Holder holder, Resource resource) {
        return permissionsRepo.removeByHolderAndResource(holder, resource)
                .collect(Collectors.toSet())
                .flatMap(permissions -> eventPublisher.publish(removed(permissions)));
    }

}
