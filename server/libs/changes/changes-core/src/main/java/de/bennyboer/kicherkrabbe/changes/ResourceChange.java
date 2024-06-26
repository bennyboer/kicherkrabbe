package de.bennyboer.kicherkrabbe.changes;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ResourceChange {

    ResourceChangeType type;

    Set<ResourceId> affected;

    Map<String, Object> payload;

    public static ResourceChange of(ResourceChangeType type, Set<ResourceId> affected, Map<String, Object> payload) {
        notNull(type, "Resource change type must be given");
        notNull(affected, "Affected resource IDs must be given");
        notNull(payload, "Payload must be given");

        return new ResourceChange(type, affected, payload);
    }

}
