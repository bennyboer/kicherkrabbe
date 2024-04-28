package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PermissionId {

    String value;

    public static PermissionId of(String value) {
        notNull(value, "Permission ID must be given");
        check(!value.isBlank(), "Permission ID must not be blank");

        return new PermissionId(value);
    }

    public static PermissionId create() {
        return of(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "PermissionId(%s)".formatted(value);
    }

}
