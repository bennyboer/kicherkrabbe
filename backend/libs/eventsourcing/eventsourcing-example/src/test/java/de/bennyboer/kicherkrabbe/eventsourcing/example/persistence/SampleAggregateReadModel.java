package de.bennyboer.kicherkrabbe.eventsourcing.example.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SampleAggregateReadModel implements VersionedReadModel<String> {

    String id;

    Version version;

    String title;

    String description;

    public static SampleAggregateReadModel of(String id, Version version, String title, String description) {
        notNull(id, "ID must be given");
        notNull(version, "Version must be given");
        notNull(title, "Title must be given");
        notNull(description, "Description must be given");

        return new SampleAggregateReadModel(id, version, title, description);
    }

}
