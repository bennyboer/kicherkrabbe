package de.bennyboer.kicherkrabbe.permissions.persistence.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

/**
 * Permissions-Queries are putting a lot of load on the database as they are called frequently.
 * Generally, every call to the backend needs to check permissions before processing the request!
 * This cache is intended to reduce the load by caching a number of last-needed permissions in-memory.
 */
public class CacheablePermissionsRepo implements PermissionsRepo {

    private final PermissionsRepo delegate;

    private final Cache<Permission, Boolean> cache;

    public CacheablePermissionsRepo(PermissionsRepo delegate, Config config) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(config.getMaximumSize())
                .expireAfterWrite(config.getExpireAfterWrite())
                .build();
    }

    @Override
    public Mono<Permission> insert(Permission permission) {
        return delegate.insert(permission);
    }

    @Override
    public Flux<Permission> insert(Collection<Permission> permissions) {
        return delegate.insert(permissions);
    }

    @Override
    public Mono<Boolean> hasPermission(Permission permission) {
        return Mono.justOrEmpty(cache.getIfPresent(permission))
                .switchIfEmpty(delegate.hasPermission(permission)
                        .doOnNext(hasPermission -> cache.put(permission, hasPermission)));
    }

    @Override
    public Flux<Permission> findPermissionsByHolder(Holder holder) {
        return delegate.findPermissionsByHolder(holder);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResourceType(Holder holder, ResourceType resourceType) {
        return delegate.findPermissionsByHolderAndResourceType(holder, resourceType);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResourceTypeAndAction(
            Holder holder,
            ResourceType resourceType,
            Action action
    ) {
        return delegate.findPermissionsByHolderAndResourceTypeAndAction(holder, resourceType, action);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResource(Holder holder, Resource resource) {
        return delegate.findPermissionsByHolderAndResource(holder, resource);
    }

    @Override
    public Flux<Permission> removeByHolder(Holder holder) {
        return delegate.removeByHolder(holder)
                .doOnNext(cache::invalidate);
    }

    @Override
    public Flux<Permission> removeByResource(Resource resource) {
        return delegate.removeByResource(resource)
                .doOnNext(cache::invalidate);
    }

    @Override
    public Flux<Permission> removeByHolderAndResource(Holder holder, Resource resource) {
        return delegate.removeByHolderAndResource(holder, resource)
                .doOnNext(cache::invalidate);
    }

    @Override
    public Mono<Permission> removeByPermission(Permission permission) {
        return delegate.removeByPermission(permission)
                .doOnNext(cache::invalidate);
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class Config {

        long maximumSize;

        /**
         * A short duration is recommended to avoid stale date.
         * For example when permissions are taken away from a user, the cache should not keep the old permissions for
         * too long.
         */
        Duration expireAfterWrite;

    }

}
