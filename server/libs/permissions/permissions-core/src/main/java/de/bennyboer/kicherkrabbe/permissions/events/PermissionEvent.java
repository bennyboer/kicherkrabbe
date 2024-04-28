package de.bennyboer.kicherkrabbe.permissions.events;

import de.bennyboer.kicherkrabbe.permissions.Permission;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.permissions.events.PermissionEventType.ADDED;
import static de.bennyboer.kicherkrabbe.permissions.events.PermissionEventType.REMOVED;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PermissionEvent {

    PermissionEventType type;

    Set<Permission> permissions;

    public static PermissionEvent added(Permission permission) {
        return added(Set.of(permission));
    }

    public static PermissionEvent added(Set<Permission> permissions) {
        return new PermissionEvent(ADDED, permissions);
    }

    public static PermissionEvent removed(Permission permission) {
        return removed(Set.of(permission));
    }

    public static PermissionEvent removed(Set<Permission> permissions) {
        return new PermissionEvent(REMOVED, permissions);
    }

}
