package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TopicDetails {

    TopicId id;

    Version version;

    TopicName name;

    Instant createdAt;

    public static TopicDetails of(TopicId id, Version version, TopicName name, Instant createdAt) {
        notNull(id, "Topic ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(createdAt, "Creation date must be given");

        return new TopicDetails(id, version, name, createdAt);
    }

}
