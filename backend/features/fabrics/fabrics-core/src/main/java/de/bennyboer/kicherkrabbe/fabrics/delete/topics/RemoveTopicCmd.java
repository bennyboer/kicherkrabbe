package de.bennyboer.kicherkrabbe.fabrics.delete.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveTopicCmd implements Command {

    TopicId topicId;

    public static RemoveTopicCmd of(TopicId topicId) {
        notNull(topicId, "Topic ID to remove must be given");

        return new RemoveTopicCmd(topicId);
    }

}

