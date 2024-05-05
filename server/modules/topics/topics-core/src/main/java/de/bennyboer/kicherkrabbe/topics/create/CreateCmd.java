package de.bennyboer.kicherkrabbe.topics.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.topics.TopicName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    TopicName name;

    public static CreateCmd of(TopicName name) {
        notNull(name, "Topic name must be given");

        return new CreateCmd(name);
    }

}
