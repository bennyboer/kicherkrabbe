package de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.telegram.settings.ApiToken;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class BotApiTokenUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("BOT_API_TOKEN_UPDATED");

    public static final Version VERSION = Version.zero();

    ApiToken apiToken;

    public static BotApiTokenUpdatedEvent of(ApiToken apiToken) {
        notNull(apiToken, "API token must be given");

        return new BotApiTokenUpdatedEvent(apiToken);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
