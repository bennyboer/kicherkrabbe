package de.bennyboer.kicherkrabbe.inquiries.persistence.requests;

import de.bennyboer.kicherkrabbe.inquiries.EMail;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Request {

    RequestId id;

    EMail mail;

    @Nullable
    String ipAddress;

    Instant createdAt;

    public static Request of(
            RequestId id,
            EMail mail,
            @Nullable String ipAddress,
            Instant createdAt
    ) {
        notNull(id, "Request ID must be given");
        notNull(mail, "Mail must be given");
        notNull(createdAt, "Created at must be given");

        return new Request(id, mail, ipAddress, createdAt);
    }

    public Optional<String> getIpAddress() {
        return Optional.ofNullable(ipAddress);
    }

}
