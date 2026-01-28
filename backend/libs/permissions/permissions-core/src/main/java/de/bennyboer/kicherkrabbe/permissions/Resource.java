package de.bennyboer.kicherkrabbe.permissions;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Resource {

    ResourceType type;

    /**
     * The resource ID on which the action is allowed.
     * If null the action is allowed on no specific resource.
     * This makes sense for actions that create new instances of a resource as there is no specific resource yet.
     * Note that passing null does not mean that the action is allowed on all resources!
     */
    @Nullable
    ResourceId id;

    public static Resource of(ResourceType type, @Nullable ResourceId id) {
        notNull(type, "Resource type must be given");

        return new Resource(type, id);
    }

    public static Resource ofType(ResourceType type) {
        return of(type, null);
    }

    public Optional<ResourceId> getId() {
        return Optional.ofNullable(id);
    }

    @Override
    public String toString() {
        return String.format("Resource(%s, %s)", type, id);
    }

}
