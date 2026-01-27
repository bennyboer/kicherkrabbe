package de.bennyboer.kicherkrabbe.mailbox.mail;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.mailbox.mail.OriginType.INQUIRY;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Origin {

    OriginType type;

    OriginId id;

    public static Origin of(OriginType type, OriginId id) {
        notNull(type, "Origin type must be given");
        notNull(id, "Origin ID must be given");

        return new Origin(type, id);
    }

    public static Origin inquiry(OriginId id) {
        return new Origin(INQUIRY, id);
    }

    @Override
    public String toString() {
        return "Origin(type=%s, id=%s)".formatted(type, id);
    }

}
