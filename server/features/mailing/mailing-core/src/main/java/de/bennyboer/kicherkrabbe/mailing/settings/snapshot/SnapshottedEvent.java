package de.bennyboer.kicherkrabbe.mailing.settings.snapshot;

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
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    RateLimitSettings rateLimit;

    MailgunSettings mailgun;

    public static SnapshottedEvent of(RateLimitSettings rateLimit, MailgunSettings mailgun) {
        notNull(rateLimit, "Rate limit settings must be given");
        notNull(mailgun, "Mailgun settings must be given");

        return new SnapshottedEvent(rateLimit, mailgun);
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public boolean isSnapshot() {
        return true;
    }

}
