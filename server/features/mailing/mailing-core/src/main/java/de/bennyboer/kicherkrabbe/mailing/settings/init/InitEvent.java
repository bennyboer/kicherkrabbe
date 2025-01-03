package de.bennyboer.kicherkrabbe.mailing.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunSettings;
import de.bennyboer.kicherkrabbe.mailing.settings.RateLimitSettings;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitEvent implements Event {

    public static final EventName NAME = EventName.of("INITIALIZED");

    public static final Version VERSION = Version.zero();

    RateLimitSettings rateLimit;

    MailgunSettings mailgun;

    public static InitEvent of(RateLimitSettings rateLimit, MailgunSettings mailgun) {
        notNull(rateLimit, "Rate limit settings must be given");
        notNull(mailgun, "Mailgun settings must be given");

        return new InitEvent(rateLimit, mailgun);
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
