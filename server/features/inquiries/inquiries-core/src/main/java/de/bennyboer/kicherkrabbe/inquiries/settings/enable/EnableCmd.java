package de.bennyboer.kicherkrabbe.inquiries.settings.enable;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EnableCmd implements Command {

    public static EnableCmd of() {
        return new EnableCmd();
    }

}
