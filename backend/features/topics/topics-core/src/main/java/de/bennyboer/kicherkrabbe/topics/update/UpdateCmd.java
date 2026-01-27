package de.bennyboer.kicherkrabbe.topics.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.topics.TopicName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateCmd implements Command {

    TopicName name;

    public static UpdateCmd of(TopicName name) {
        notNull(name, "Topic name must be given");

        return new UpdateCmd(name);
    }

}
