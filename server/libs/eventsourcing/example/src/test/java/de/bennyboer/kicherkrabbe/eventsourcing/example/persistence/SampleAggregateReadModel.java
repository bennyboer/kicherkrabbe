package de.bennyboer.kicherkrabbe.eventsourcing.example.persistence;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SampleAggregateReadModel {

    String id;

    String title;

    String description;

    public static SampleAggregateReadModel of(String id, String title, String description) {
        notNull(id, "ID must be given");
        notNull(title, "Title must be given");
        notNull(description, "Description must be given");

        return new SampleAggregateReadModel(id, title, description);
    }

}
