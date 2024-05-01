package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * A permission is a relation between a holder and a specific resource.
 * More precisely it defines an action a holder is allowed to perform on a given resource.
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class Permission {

    Holder holder;

    Action action;

    Resource resource;

    public static Permission of(
            Holder holder,
            Action action,
            Resource resource
    ) {
        notNull(holder, "Holder must be given");
        notNull(action, "Action must be given");
        notNull(resource, "Resource must be given");

        return new Permission(
                holder,
                action,
                resource
        );
    }

    @Override
    public String toString() {
        return String.format(
                "Permission(holder=%s, action=%s, resource=%s)",
                holder,
                action,
                resource
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
                    holder,
                    action,
                    Resource.ofType(resourceType)
            );
        }

        public Permission on(Resource resource) {
            return Permission.of(
                    holder,
                    action,
                    resource
            );
        }

    }

}
