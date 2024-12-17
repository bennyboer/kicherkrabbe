package de.bennyboer.kicherkrabbe.inquiries.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitCmd implements Command {

    public static InitCmd of() {
        return new InitCmd();
    }

}
