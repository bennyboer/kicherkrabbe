package de.bennyboer.kicherkrabbe.eventsourcing.example.commands;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateDescriptionCmd implements Command {

    String description;

    public static UpdateDescriptionCmd of(String description) {
        notNull(description, "Description must be given");
        check(!description.isBlank(), "Description must not be blank");

        return new UpdateDescriptionCmd(description);
    }

}
