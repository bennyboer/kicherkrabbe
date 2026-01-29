package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.inquiries.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupInquiry implements VersionedReadModel<InquiryId> {

    InquiryId id;

    Version version;

    RequestId requestId;

    Sender sender;

    Subject subject;

    Message message;

    Fingerprint fingerprint;

    Instant createdAt;

    public static LookupInquiry of(
            InquiryId id,
            Version version,
            RequestId requestId,
            Sender sender,
            Subject subject,
            Message message,
            Fingerprint fingerprint,
            Instant createdAt
    ) {
        notNull(id, "Inquiry ID must be given");
        notNull(version, "Version must be given");
        notNull(requestId, "Request ID must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(message, "Message must be given");
        notNull(fingerprint, "Fingerprint must be given");
        notNull(createdAt, "Created at must be given");

        return new LookupInquiry(
                id,
                version,
                requestId,
                sender,
                subject,
                message,
                fingerprint,
                createdAt
        );
    }

}
