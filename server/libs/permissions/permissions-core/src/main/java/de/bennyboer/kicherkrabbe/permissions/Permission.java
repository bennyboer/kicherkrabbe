package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * A permission is a relation between a holder and a specific resource.
 * More precisely it defines an action a holder is allowed to perform on a given resource.
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class Permission {

    PermissionId id;

    Holder holder;

    Action action;

    Resource resource;

    Instant createdAt;

    public static Permission of(
            PermissionId id,
            Holder holder,
            Action action,
            Resource resource,
            Instant createdAt
    ) {
        notNull(id, "Id must be given");
        notNull(holder, "Holder must be given");
        notNull(action, "Action must be given");
        notNull(resource, "Resource must be given");
        notNull(createdAt, "Creation date must be given");

        return new Permission(
                id,
                holder,
                action,
                resource,
                createdAt
        );
    }

    @Override
    public String toString() {
        return String.format(
                "Permission(holder=%s, action=%s, resource=%s, createdAt=%s)",
                holder,
                action,
                resource,
                createdAt
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * The preferred way to create a permission.
     * <pre>
     * var permission = Permission.builder()
     *                            .holder(holder)
     *                            .isAllowedTo(ADD_NODES)
     *                            .on(structureResource);
     * </pre>
     */
    public static class Builder {

        private Holder holder;

        private Action action;

        public Builder holder(Holder holder) {
            this.holder = holder;
            return this;
        }

        public Builder isAllowedTo(Action action) {
            this.action = action;
            return this;
        }

        public Permission onType(ResourceType resourceType) {
            return Permission.of(
                    PermissionId.create(),
                    holder,
                    action,
                    Resource.ofType(resourceType),
                    Instant.now()
            );
        }

        public Permission on(Resource resource) {
            return Permission.of(
                    PermissionId.create(),
                    holder,
                    action,
                    resource,
                    Instant.now()
            );
        }

    }

}
