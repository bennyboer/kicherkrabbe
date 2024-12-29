package de.bennyboer.kicherkrabbe.telegram.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.telegram.settings.BotSettings;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitCmd implements Command {

    BotSettings botSettings;

    public static InitCmd of(BotSettings botSettings) {
        notNull(botSettings, "Bot settings must be given");

        return new InitCmd(botSettings);
    }

}
