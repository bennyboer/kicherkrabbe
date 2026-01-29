package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.mailing.mail.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupMail implements VersionedReadModel<MailId> {

    MailId id;

    Version version;

    Sender sender;

    Set<Receiver> receivers;

    Subject subject;

    Text text;

    Instant sentAt;

    public static LookupMail of(
            MailId id,
            Version version,
            Sender sender,
            Set<Receiver> receivers,
            Subject subject,
            Text text,
            Instant sentAt
    ) {
        notNull(id, "Id must be given");
        notNull(version, "Version must be given");
        notNull(sender, "Sender must be given");
        notNull(receivers, "Receivers must be given");
        notNull(subject, "Subject must be given");
        notNull(text, "Text must be given");
        notNull(sentAt, "Sent at must be given");

        return new LookupMail(
                id,
                version,
                sender,
                receivers,
                subject,
                text,
                sentAt
        );
    }

}
