package de.bennyboer.kicherkrabbe.changes;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ResourceChangeType {

    public static final ResourceChangeType PERMISSIONS_ADDED = ResourceChangeType.of("PERMISSIONS_ADDED");

    public static final ResourceChangeType PERMISSIONS_REMOVED = ResourceChangeType.of("PERMISSIONS_REMOVED");

    String value;

    public static ResourceChangeType of(String value) {
        notNull(value, "Resource change type must be given");
        check(!value.isBlank(), "Resource change type must not be blank");

        return new ResourceChangeType(value);
    }

    @Override
    public String toString() {
        return "ResourceChangeType(%s)".formatted(value);
    }

}
