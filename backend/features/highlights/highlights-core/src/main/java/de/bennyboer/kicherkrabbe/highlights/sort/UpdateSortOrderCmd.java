package de.bennyboer.kicherkrabbe.highlights.sort;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateSortOrderCmd implements Command {

    long sortOrder;

    public static UpdateSortOrderCmd of(long sortOrder) {
        return new UpdateSortOrderCmd(sortOrder);
    }

}
