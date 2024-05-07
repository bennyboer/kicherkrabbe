package de.bennyboer.kicherkrabbe.changes;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ResourceType {

    String value;

    public static ResourceType of(String value) {
        notNull(value, "Resource type must be given");
        check(!value.isBlank(), "Resource type must not be blank");

        return new ResourceType(value);
    }

    @Override
    public String toString() {
        return "ResourceType(%s)".formatted(value);
    }

}
