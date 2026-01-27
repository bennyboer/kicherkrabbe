package de.bennyboer.kicherkrabbe.fabrics.persistence.topics;

import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Topic {

    TopicId id;

    TopicName name;

    public static Topic of(TopicId id, TopicName name) {
        notNull(id, "Topic ID must be given");
        notNull(name, "Topic name must be given");

        return new Topic(id, name);
    }

}
