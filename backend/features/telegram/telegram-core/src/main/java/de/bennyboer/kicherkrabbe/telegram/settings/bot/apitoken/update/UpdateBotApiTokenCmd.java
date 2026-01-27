package de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateBotApiTokenCmd implements Command {

    ApiToken apiToken;

    public static UpdateBotApiTokenCmd of(ApiToken apiToken) {
        notNull(apiToken, "API token must be given");

        return new UpdateBotApiTokenCmd(apiToken);
    }

}
