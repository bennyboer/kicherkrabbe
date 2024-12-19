package de.bennyboer.kicherkrabbe.inquiries.send;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.inquiries.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SendCmd implements Command {

    RequestId requestId;

    Sender sender;

    Subject subject;

    Message message;

    Fingerprint fingerprint;

    public static SendCmd of(
            RequestId requestId,
            Sender sender,
            Subject subject,
            Message message,
            Fingerprint fingerprint
    ) {
        notNull(requestId, "Request ID must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(message, "Message must be given");
        notNull(fingerprint, "Fingerprint must be given");

        return new SendCmd(requestId, sender, subject, message, fingerprint);
    }

}
