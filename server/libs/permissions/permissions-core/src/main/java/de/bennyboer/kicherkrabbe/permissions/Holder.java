package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.permissions.HolderType.GROUP;
import static de.bennyboer.kicherkrabbe.permissions.HolderType.USER;
import static lombok.AccessLevel.PRIVATE;

/**
 * The holder of a permission.
 * For example a permission might be held by a specific user or a group (unauthorized users, admins, etc.).
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class Holder {

    HolderType type;

    HolderId id;

    public static Holder user(HolderId id) {
        return new Holder(USER, id);
    }

    public static Holder group(HolderId id) {
        return new Holder(GROUP, id);
    }

    @Override
    public String toString() {
        return "Holder(" + type + ", " + id + ")";
    }

}
