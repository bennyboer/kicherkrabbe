package de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.mailing.settings.ApiToken;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateMailgunApiTokenCmd implements Command {

    ApiToken apiToken;

    public static UpdateMailgunApiTokenCmd of(ApiToken apiToken) {
        notNull(apiToken, "API token must be given");

        return new UpdateMailgunApiTokenCmd(apiToken);
    }

}
