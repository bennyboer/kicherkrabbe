package de.bennyboer.kicherkrabbe.permissions;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ResourceType {

    String name;

    public static ResourceType of(String name) {
        notNull(name, "Resource type name must be given");
        check(!name.isBlank(), "Resource type name must not be blank");

        return new ResourceType(name);
    }

    @Override
    public String toString() {
        return String.format("ResourceType(%s)", name);
    }

}
