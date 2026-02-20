package de.bennyboer.kicherkrabbe.offers.archive;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ArchiveCmd implements Command {

    public static ArchiveCmd of() {
        return new ArchiveCmd();
    }

}
