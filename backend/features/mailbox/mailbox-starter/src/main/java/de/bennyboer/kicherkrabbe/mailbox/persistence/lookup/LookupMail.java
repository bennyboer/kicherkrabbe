package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.mailbox.mail.*;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupMail implements VersionedReadModel<MailId> {

    MailId id;

    Version version;

    Origin origin;

    Sender sender;

    Subject subject;

    Content content;

    Instant receivedAt;

    Status status;

    @Nullable
    Instant readAt;

    public static LookupMail of(
            MailId id,
            Version version,
            Origin origin,
            Sender sender,
            Subject subject,
            Content content,
            Instant receivedAt,
            Status status,
            @Nullable Instant readAt
    ) {
        notNull(id, "Mail ID must be given");
        notNull(version, "Version must be given");
        notNull(origin, "Origin must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(content, "Content must be given");
        notNull(receivedAt, "Received at must be given");
        notNull(status, "Status must be given");

        return new LookupMail(
                id,
                version,
                origin,
                sender,
                subject,
                content,
                receivedAt,
                status,
                readAt
        );
    }

    public Optional<Instant> getReadAt() {
        return Optional.ofNullable(readAt);
    }

}
