package de.bennyboer.kicherkrabbe.inquiries.settings.disable;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DisableCmd implements Command {

    public static DisableCmd of() {
        return new DisableCmd();
    }

}
