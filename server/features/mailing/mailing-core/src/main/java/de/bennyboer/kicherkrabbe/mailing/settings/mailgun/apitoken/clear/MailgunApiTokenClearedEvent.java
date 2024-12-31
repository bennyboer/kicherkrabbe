package de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MailgunApiTokenClearedEvent implements Event {

    public static final EventName NAME = EventName.of("MAILGUN_API_TOKEN_CLEARED");

    public static final Version VERSION = Version.zero();

    public static MailgunApiTokenClearedEvent of() {
        return new MailgunApiTokenClearedEvent();
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
