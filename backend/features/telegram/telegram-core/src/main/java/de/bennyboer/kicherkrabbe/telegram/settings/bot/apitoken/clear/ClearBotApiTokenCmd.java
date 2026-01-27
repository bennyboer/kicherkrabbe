package de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.clear;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ClearBotApiTokenCmd implements Command {

    public static ClearBotApiTokenCmd of() {
        return new ClearBotApiTokenCmd();
    }

}
