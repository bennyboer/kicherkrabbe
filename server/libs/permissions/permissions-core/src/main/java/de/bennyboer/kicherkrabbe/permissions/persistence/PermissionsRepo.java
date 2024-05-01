package de.bennyboer.kicherkrabbe.permissions.persistence;

import de.bennyboer.kicherkrabbe.permissions.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

/**
 * Data-Access interface for permissions.
 * It is intended to store and retrieve permissions from a persistent storage.
 */
public interface PermissionsRepo {

    Mono<Permission> insert(Permission permission);

    default Flux<Permission> insert(Permission... permissions) {
        return insert(Set.of(permissions));
    }

    Flux<Permission> insert(Collection<Permission> permissions);

    Mono<Boolean> hasPermission(Permission permission);

    /**
     * Find all permissions for a specific holder.
     * This may be useful if you want to display all permissions for a holder.
     */
    Flux<Permission> findPermissionsByHolder(Holder holder);

    /**
     * Find all permissions for a specific holder and resource type.
     * This may be useful if you want to display all permissions for a holder on a specific resource type.
     * For example if you want to query all accessible projects for a holder.
     */
    Flux<Permission> findPermissionsByHolderAndResourceType(Holder holder, ResourceType resourceType);

    /**
     * Find all permissions for a specific holder, resource type and action.
     * This may be useful if you want to display all permissions for a holder on a specific resource type and action.
     * For example if you want to query all readable projects for a holder.
     */
    Flux<Permission> findPermissionsByHolderAndResourceTypeAndAction(
            Holder holder,
            ResourceType resourceType,
            Action action
    );

    /**
     * Find all permissions for a specific resource.
     * This may be useful if you want to display all permissions to a resource for a holder.
     */
    Flux<Permission> findPermissionsByHolderAndResource(Holder holder, Resource resource);

    /**
     * Remove all permissions for a specific holder.
     * This is useful when a holder is deleted and all permissions should be removed.
     */
    Flux<Permission> removeByHolder(Holder holder);

    /**
     * Remove all permissions for a specific resource.
     * This is useful when a resource is deleted and all permissions to it should be removed.
     */
    Flux<Permission> removeByResource(Resource resource);

    /**
     * Remove all permissions for a specific holder and resource.
     * This is useful when all permissions to a resource should be removed for a holder.
     */
    Flux<Permission> removeByHolderAndResource(Holder holder, Resource resource);

    /**
     * Remove a specific permission.
     */
    Mono<Permission> removeByPermission(Permission permission);

}
