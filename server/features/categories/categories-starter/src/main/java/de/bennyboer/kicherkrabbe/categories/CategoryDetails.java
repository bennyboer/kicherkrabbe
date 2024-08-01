package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CategoryDetails {

    CategoryId id;

    Version version;

    CategoryName name;

    CategoryGroup group;

    Instant createdAt;

    public static CategoryDetails of(
            CategoryId id,
            Version version,
            CategoryName name,
            CategoryGroup group,
            Instant createdAt
    ) {
        notNull(id, "Category ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(group, "Group must be given");
        notNull(createdAt, "Creation date must be given");

        return new CategoryDetails(id, version, name, group, createdAt);
    }

}
