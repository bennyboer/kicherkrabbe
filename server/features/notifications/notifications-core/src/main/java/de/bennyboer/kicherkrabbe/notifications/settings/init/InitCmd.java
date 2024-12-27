package de.bennyboer.kicherkrabbe.notifications.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.notifications.settings.SystemSettings;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitCmd implements Command {

    SystemSettings systemSettings;

    public static InitCmd of(SystemSettings systemSettings) {
        notNull(systemSettings, "System settings must be given");

        return new InitCmd(systemSettings);
    }

}
