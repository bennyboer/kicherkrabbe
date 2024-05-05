package de.bennyboer.kicherkrabbe.fabrics.update.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateTopicsCmd implements Command {

    Set<TopicId> topics;

    public static UpdateTopicsCmd of(Set<TopicId> topics) {
        notNull(topics, "Topics must be given");

        return new UpdateTopicsCmd(topics);
    }

}
