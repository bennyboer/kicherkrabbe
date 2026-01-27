package de.bennyboer.kicherkrabbe.eventsourcing.example.commands;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateTitleCmd implements Command {

    String title;

    public static UpdateTitleCmd of(String title) {
        notNull(title, "title must be given");
        check(!title.isBlank(), "title must not be blank");

        return new UpdateTitleCmd(title);
    }

}
