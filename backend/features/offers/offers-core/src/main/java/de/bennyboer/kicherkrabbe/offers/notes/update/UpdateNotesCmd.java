package de.bennyboer.kicherkrabbe.offers.notes.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.offers.Notes;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateNotesCmd implements Command {

    Notes notes;

    public static UpdateNotesCmd of(Notes notes) {
        notNull(notes, "Notes must be given");

        return new UpdateNotesCmd(notes);
    }

}
