package de.bennyboer.kicherkrabbe.mailing.mail.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailing.MailingService;
import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    Sender sender;

    Set<Receiver> receivers;

    Subject subject;

    Text text;

    MailingService mailingService;

    Instant sentAt;

    @Nullable
    Instant deletedAt;

    public static SnapshottedEvent of(
            Sender sender,
            Set<Receiver> receivers,
            Subject subject,
            Text text,
            MailingService mailingService,
            Instant sentAt,
            @Nullable Instant deletedAt
    ) {
        notNull(sender, "Sender must be given");
        notNull(receivers, "Receivers must be given");
        notNull(subject, "Subject must be given");
        notNull(text, "Text must be given");
        notNull(mailingService, "Mailing service must be given");
        notNull(sentAt, "Sent at must be given");

        return new SnapshottedEvent(sender, receivers, subject, text, mailingService, sentAt, deletedAt);
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

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

}
