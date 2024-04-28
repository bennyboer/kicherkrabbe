package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ResourceId {

    String value;

    public static ResourceId of(String value) {
        notNull(value, "Resource ID must be given");
        check(!value.isBlank(), "Resource ID must not be blank");

        return new ResourceId(value);
    }

    @Override
    public String toString() {
        return String.format("ResourceId(%s)", value);
    }

}
