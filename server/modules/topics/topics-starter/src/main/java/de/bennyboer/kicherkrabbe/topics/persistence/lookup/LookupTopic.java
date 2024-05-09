package de.bennyboer.kicherkrabbe.topics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.topics.TopicId;
import de.bennyboer.kicherkrabbe.topics.TopicName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupTopic {

    TopicId id;

    Version version;

    TopicName name;

    Instant createdAt;

    public static LookupTopic of(TopicId id, Version version, TopicName name, Instant createdAt) {
        notNull(id, "Topic ID must be given");
        notNull(version, "Version must be given");
        notNull(name, "Name must be given");
        notNull(createdAt, "Creation date must be given");

        return new LookupTopic(id, version, name, createdAt);
    }

}
